// Debate Arena - vanilla JavaScript client for the Spring Boot REST API.
'use strict';

const state = {
    participants: [],
    topics: [],
    sessions: [],
    currentSession: null,
    selectedClaimId: null,
    charts: {},
};

// ------------------------------------------------------------------ helpers

async function api(path, options = {}) {
    const res = await fetch('/api' + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
    });
    const text = await res.text();
    const body = text ? JSON.parse(text) : null;
    if (!res.ok) {
        const details = body?.details?.length ? ': ' + body.details.join(', ') : '';
        throw new Error((body?.message || res.statusText) + details);
    }
    return body;
}

const post = (path, data) => api(path, { method: 'POST', body: JSON.stringify(data) });

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]));
}

function toast(message, isError = false) {
    const el = document.getElementById('toast');
    el.textContent = message;
    el.className = 'toast show' + (isError ? ' error' : '');
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => (el.className = 'toast'), 3000);
}

function options(items, valueKey, labelFn, selected) {
    return items.map(i =>
        `<option value="${i[valueKey]}" ${i[valueKey] === selected ? 'selected' : ''}>${escapeHtml(labelFn(i))}</option>`
    ).join('');
}

function debounce(fn, ms) {
    let t;
    return (...args) => {
        clearTimeout(t);
        t = setTimeout(() => fn(...args), ms);
    };
}

function drawChart(key, canvasId, config) {
    state.charts[key]?.destroy();
    state.charts[key] = new Chart(document.getElementById(canvasId), config);
}

const fmt = n => (n === null || n === undefined ? '-' : Number(n).toFixed(1));

// ------------------------------------------------------------------ tabs

document.querySelectorAll('.tab').forEach(btn => btn.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(b => b.classList.toggle('active', b === btn));
    document.querySelectorAll('.panel').forEach(p => p.classList.toggle('active', p.id === 'tab-' + btn.dataset.tab));
    if (btn.dataset.tab === 'claims') loadClaimsTab();
    if (btn.dataset.tab === 'leaderboard') loadLeaderboard();
}));

// ------------------------------------------------------------------ base data

async function loadBaseData() {
    [state.participants, state.topics, state.sessions] = await Promise.all([
        api('/participants'), api('/topics'), api('/sessions'),
    ]);

    document.getElementById('arg-participant').innerHTML =
        options(state.participants, 'id', p => p.name);
    document.querySelectorAll('.topic-options').forEach(sel =>
        (sel.innerHTML = options(state.topics, 'id', t => t.title)));
    document.getElementById('claims-topic-select').innerHTML =
        options(state.topics, 'id', t => t.title);

    const sessionSelect = document.getElementById('session-select');
    const previous = state.currentSession?.session.id;
    sessionSelect.innerHTML = options(state.sessions, 'id', s => `${s.title}  (${s.topicTitle})`, previous);
    if (state.sessions.length) await loadSession(Number(sessionSelect.value));
}

// ------------------------------------------------------------------ debate tab

async function loadSession(id) {
    state.currentSession = await api('/sessions/' + id);
    document.getElementById('arg-claim').innerHTML =
        options(state.currentSession.claims, 'id', c => c.text);
    renderArguments();
}

function renderArguments() {
    const list = document.getElementById('argument-list');
    const args = state.currentSession.arguments;
    if (!args.length) {
        list.innerHTML = '<p class="muted">No arguments yet - be the first.</p>';
        return;
    }
    list.innerHTML = args.map(a => {
        const raters = state.participants.filter(p => p.id !== a.participantId);
        return `
        <article class="argument ${a.stance}">
            <div class="meta">
                <strong>${escapeHtml(a.participantName)}</strong>
                <span class="badge ${a.stance}">${a.stance}</span>
                <span class="badge ${a.hasExample ? 'on' : ''}">example</span>
                <span class="badge ${a.hasStatistic ? 'on' : ''}">statistic</span>
                <span class="badge ${a.hasSource ? 'on' : ''}">source</span>
                <span class="muted">quality ${a.qualityScore} · rating ${fmt(a.avgRating)} (${a.ratingCount}) · score ${fmt(a.combinedScore)}</span>
            </div>
            <div class="claim">on: “${escapeHtml(a.claimText)}”</div>
            <p>${escapeHtml(a.content)}</p>
            <form class="rate" data-argument="${a.id}">
                <select name="raterId">${options(raters, 'id', p => p.name)}</select>
                <select name="score">${[5, 4, 3, 2, 1].map(s => `<option value="${s}">${'★'.repeat(s)}</option>`).join('')}</select>
                <button class="btn" type="submit">Rate</button>
            </form>
        </article>`;
    }).join('');
}

document.getElementById('session-select').addEventListener('change', e => loadSession(Number(e.target.value)));

document.getElementById('argument-list').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const data = Object.fromEntries(new FormData(form));
    try {
        await post(`/arguments/${form.dataset.argument}/ratings`, {
            raterId: Number(data.raterId), score: Number(data.score),
        });
        toast('Rating saved');
        await loadSession(state.currentSession.session.id);
    } catch (err) {
        toast(err.message, true);
    }
});

const previewQuality = debounce(async text => {
    const box = document.getElementById('quality-preview');
    if (text.trim().length < 3) {
        box.innerHTML = 'Start typing to see a live quality assessment.';
        return;
    }
    try {
        const q = await post('/arguments/assess', { text });
        box.innerHTML = `
            <span class="score">${q.score}/100</span>
            <span class="badge ${q.hasExample ? 'on' : ''}">example</span>
            <span class="badge ${q.hasStatistic ? 'on' : ''}">statistic</span>
            <span class="badge ${q.hasSource ? 'on' : ''}">source</span>
            ${q.feedback.length ? '<ul>' + q.feedback.map(f => `<li>${escapeHtml(f)}</li>`).join('') + '</ul>' : '<p>Looks great!</p>'}`;
    } catch (err) {
        box.textContent = err.message;
    }
}, 350);

document.getElementById('arg-content').addEventListener('input', e => previewQuality(e.target.value));

document.getElementById('argument-form').addEventListener('submit', async e => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    try {
        const saved = await post('/arguments', {
            sessionId: state.currentSession.session.id,
            claimId: Number(data.claimId),
            participantId: Number(data.participantId),
            stance: data.stance,
            content: data.content,
        });
        toast(`Argument saved - quality ${saved.qualityScore}/100`);
        e.target.reset();
        document.getElementById('quality-preview').innerHTML = 'Start typing to see a live quality assessment.';
        await loadSession(state.currentSession.session.id);
    } catch (err) {
        toast(err.message, true);
    }
});

// ------------------------------------------------------------------ claims tab

async function loadClaimsTab() {
    const topicId = Number(document.getElementById('claims-topic-select').value);
    if (!topicId) return;
    const claims = await api(`/analytics/topics/${topicId}/claims`);

    drawChart('claims', 'claims-chart', {
        type: 'bar',
        data: {
            labels: claims.map(c => c.claimText),
            datasets: [{
                label: 'Strength (0 = refuted, 100 = proven)',
                data: claims.map(c => c.analysis.strength),
                backgroundColor: claims.map(c => c.analysis.strength >= 50 ? '#2f9e44' : '#e03131'),
            }],
        },
        options: { indexAxis: 'y', scales: { x: { min: 0, max: 100 } }, plugins: { legend: { display: false } } },
    });

    document.getElementById('claims-list').innerHTML = claims.map(c => `
        <li data-claim="${c.claimId}" class="${c.claimId === state.selectedClaimId ? 'selected' : ''}">
            <span>${escapeHtml(c.claimText)}</span>
            <span class="verdict-${c.analysis.verdict}">${c.analysis.verdict} · ${fmt(c.analysis.strength)}
                <span class="muted">(${c.analysis.proCount} pro / ${c.analysis.conCount} con)</span></span>
        </li>`).join('');

    const toSelect = claims.find(c => c.claimId === state.selectedClaimId) || claims[0];
    if (toSelect) await loadClaimDetail(toSelect.claimId);
}

async function loadClaimDetail(claimId) {
    state.selectedClaimId = claimId;
    document.querySelectorAll('#claims-list li').forEach(li =>
        li.classList.toggle('selected', Number(li.dataset.claim) === claimId));

    const [strength, ranking] = await Promise.all([
        api(`/analytics/claims/${claimId}/strength`),
        api(`/analytics/claims/${claimId}/rankings`),
    ]);

    document.getElementById('claim-detail-title').textContent = strength.claimText;
    drawChart('claimSessions', 'claim-sessions-chart', {
        type: 'line',
        data: {
            labels: strength.analysis.sessions.map(s => s.sessionTitle),
            datasets: [{ label: 'Strength per session', data: strength.analysis.sessions.map(s => s.strength), tension: 0.3 }],
        },
        options: { scales: { y: { min: 0, max: 100 } } },
    });

    document.getElementById('claim-ranking-table').innerHTML = rankingTable(ranking.rankings);
}

document.getElementById('claims-topic-select').addEventListener('change', () => {
    state.selectedClaimId = null;
    loadClaimsTab();
});
document.getElementById('claims-list').addEventListener('click', e => {
    const li = e.target.closest('li[data-claim]');
    if (li) loadClaimDetail(Number(li.dataset.claim));
});

// ------------------------------------------------------------------ leaderboard tab

function rankingTable(rows) {
    if (!rows.length) return '<tr><td class="muted">No arguments yet.</td></tr>';
    return `
        <thead><tr><th>#</th><th>Participant</th><th>Args</th><th>Avg rating</th><th>Avg quality</th><th>Score</th></tr></thead>
        <tbody>${rows.map(r => `
            <tr data-participant="${r.participantId}">
                <td>${r.rank}</td><td>${escapeHtml(r.participantName)}</td><td>${r.argumentCount}</td>
                <td>${fmt(r.avgRating)}</td><td>${fmt(r.avgQuality)}</td><td><b>${fmt(r.score)}</b></td>
            </tr>`).join('')}
        </tbody>`;
}

async function loadLeaderboard() {
    const rows = await api('/analytics/leaderboard');
    document.getElementById('leaderboard-table').innerHTML = rankingTable(rows);
    drawChart('leaderboard', 'leaderboard-chart', {
        type: 'bar',
        data: {
            labels: rows.map(r => r.participantName),
            datasets: [
                { label: 'Score', data: rows.map(r => r.score) },
                { label: 'Avg quality', data: rows.map(r => r.avgQuality) },
            ],
        },
        options: { scales: { y: { min: 0, max: 100 } } },
    });
    if (rows.length) await loadParticipantStats(rows[0].participantId);
}

async function loadParticipantStats(id) {
    const s = await api('/analytics/participants/' + id);
    const stat = (label, value) => `<div class="stat"><span class="muted">${label}</span><b>${value}</b></div>`;
    document.getElementById('participant-stats').innerHTML = `
        <h3 style="grid-column: 1 / -1">${escapeHtml(s.name)}</h3>
        ${stat('Arguments', s.argumentCount)}
        ${stat('PRO / CON', `${s.proCount} / ${s.conCount}`)}
        ${stat('Avg quality', fmt(s.avgQuality))}
        ${stat('Avg rating received', fmt(s.avgRatingReceived))}
        ${stat('Ratings given', s.ratingsGiven)}
        ${stat('Examples used', s.examplesUsed)}
        ${stat('Sources cited', s.sourcesCited)}`;
}

document.getElementById('leaderboard-table').addEventListener('click', e => {
    const tr = e.target.closest('tr[data-participant]');
    if (tr) loadParticipantStats(Number(tr.dataset.participant));
});

// ------------------------------------------------------------------ manage tab

document.querySelectorAll('#tab-manage form').forEach(form => form.addEventListener('submit', async e => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(form));
    try {
        if (form.dataset.kind === 'claim') {
            await post(`/topics/${data.topicId}/claims`, { text: data.text });
        } else {
            if (data.topicId) data.topicId = Number(data.topicId);
            await post(form.dataset.endpoint, data);
        }
        toast(`New ${form.dataset.kind} saved`);
        form.reset();
        await loadBaseData();
    } catch (err) {
        toast(err.message, true);
    }
}));

// ------------------------------------------------------------------ start

loadBaseData().catch(err => toast('Could not reach the server: ' + err.message, true));
