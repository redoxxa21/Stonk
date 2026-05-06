const API_PREFIX = '/api';
const state = {
  token: localStorage.getItem('stonk_token') || '',
  username: localStorage.getItem('stonk_username') || '',
  role: localStorage.getItem('stonk_role') || '',
  userId: localStorage.getItem('stonk_user_id') || '',
  sessionUser: null,
};

const el = {
  connectionBadge: document.getElementById('connectionBadge'),
  pingBtn: document.getElementById('pingBtn'),
  clearBtn: document.getElementById('clearBtn'),
  sessionUser: document.getElementById('sessionUser'),
  sessionUserId: document.getElementById('sessionUserId'),
  sessionToken: document.getElementById('sessionToken'),
  messageBar: document.getElementById('messageBar'),
  authResult: document.getElementById('authResult'),
  usersResult: document.getElementById('usersResult'),
  walletResult: document.getElementById('walletResult'),
  portfolioResult: document.getElementById('portfolioResult'),
  tradesResult: document.getElementById('tradesResult'),
};

const forms = {
  register: document.getElementById('registerForm'),
  login: document.getElementById('loginForm'),
  userByUsername: document.getElementById('userByUsernameForm'),
  userById: document.getElementById('userByIdForm'),
  updateUser: document.getElementById('updateUserForm'),
  deleteUser: document.getElementById('deleteUserForm'),
  walletLookup: document.getElementById('walletLookupForm'),
  walletCreate: document.getElementById('walletCreateForm'),
  walletChange: document.getElementById('walletChangeForm'),
  walletTx: document.getElementById('walletTxForm'),
  portfolioLookup: document.getElementById('portfolioLookupForm'),
  holdingLookup: document.getElementById('holdingLookupForm'),
  portfolioTrade: document.getElementById('portfolioTradeForm'),
  tradeAction: document.getElementById('tradeActionForm'),
  tradeList: document.getElementById('tradeListForm'),
  tradeDetail: document.getElementById('tradeDetailForm'),
};

function authHeaders(extra = {}) {
  const headers = { ...extra };
  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }
  return headers;
}

async function api(path, options = {}) {
  const init = {
    method: options.method || 'GET',
    headers: {
      Accept: 'application/json',
      ...authHeaders(options.headers || {}),
    },
  };

  if (options.body !== undefined) {
    init.headers['Content-Type'] = 'application/json';
    init.body = JSON.stringify(options.body);
  }

  const response = await fetch(`${API_PREFIX}${path}`, init);
  const text = await response.text();
  let payload = null;

  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = text;
    }
  }

  if (!response.ok) {
    const message = payload?.message || payload?.error || (typeof payload === 'string' ? payload : response.statusText);
    const error = new Error(message || `Request failed with status ${response.status}`);
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
}

function setMessage(type, text) {
  el.messageBar.textContent = text;
  el.messageBar.className = `message ${type}`;
  el.messageBar.classList.remove('hidden');
}

function clearMessage() {
  el.messageBar.textContent = '';
  el.messageBar.className = 'message hidden';
}

function showResult(target, value) {
  if (value === null || value === undefined) {
    target.innerHTML = '<div class="message info">No data returned.</div>';
    return;
  }

  if (Array.isArray(value)) {
    if (value.length === 0) {
      target.innerHTML = '<div class="message info">[]</div>';
      return;
    }
    target.innerHTML = renderTable(value);
    return;
  }

  if (typeof value === 'object') {
    target.innerHTML = renderTable([value]);
    return;
  }

  target.innerHTML = `<pre class="output">${escapeHtml(String(value))}</pre>`;
}

function renderTable(rows) {
  const keys = Array.from(
    rows.reduce((set, row) => {
      Object.keys(row || {}).forEach((key) => set.add(key));
      return set;
    }, new Set()),
  );

  const head = keys.map((key) => `<th>${escapeHtml(key)}</th>`).join('');
  const body = rows
    .map((row) => {
      const cells = keys
        .map((key) => `<td>${formatCell(row?.[key])}</td>`)
        .join('');
      return `<tr>${cells}</tr>`;
    })
    .join('');

  return `<table><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`;
}

function formatCell(value) {
  if (value === null || value === undefined) return '';
  if (typeof value === 'object') return `<pre class="output">${escapeHtml(JSON.stringify(value, null, 2))}</pre>`;
  return escapeHtml(String(value));
}

function escapeHtml(value) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function syncSessionUI() {
  el.sessionUser.textContent = state.username ? `${state.username}${state.role ? ` (${state.role})` : ''}` : 'No active session';
  el.sessionUserId.textContent = state.userId || '-';
  el.sessionToken.textContent = state.token ? `${state.token.slice(0, 18)}...` : 'Not stored';
  el.connectionBadge.textContent = state.token ? 'Session stored' : 'No token';
  el.connectionBadge.className = `badge ${state.token ? 'badge-success' : 'badge-neutral'}`;
}

function persistSession(session) {
  state.token = session.token || '';
  state.username = session.username || '';
  state.role = session.role || '';
  localStorage.setItem('stonk_token', state.token);
  localStorage.setItem('stonk_username', state.username);
  localStorage.setItem('stonk_role', state.role);
  syncSessionUI();
}

function setSessionUser(user) {
  state.sessionUser = user;
  state.userId = user?.id ? String(user.id) : '';
  if (state.userId) {
    localStorage.setItem('stonk_user_id', state.userId);
  } else {
    localStorage.removeItem('stonk_user_id');
  }
  syncSessionUI();
}

function hydrateUserFields(user) {
  if (!user) return;
  const ids = ['userByIdForm', 'updateUserForm', 'deleteUserForm', 'walletLookupForm', 'walletCreateForm', 'walletChangeForm', 'walletTxForm', 'portfolioLookupForm', 'holdingLookupForm', 'portfolioTradeForm', 'tradeActionForm', 'tradeListForm'];
  ids.forEach((id) => {
    const form = document.getElementById(id);
    if (form?.elements?.userId && !form.elements.userId.value) {
      form.elements.userId.value = user.id;
    }
  });
  const updateForm = forms.updateUser;
  if (updateForm.elements.id && !updateForm.elements.id.value) {
    updateForm.elements.id.value = user.id;
  }
  const deleteForm = forms.deleteUser;
  if (deleteForm.elements.id && !deleteForm.elements.id.value) {
    deleteForm.elements.id.value = user.id;
  }
  const byIdForm = forms.userById;
  if (byIdForm.elements.id && !byIdForm.elements.id.value) {
    byIdForm.elements.id.value = user.id;
  }
}

async function resolveCurrentUser() {
  if (!state.username) return null;
  const user = await api(`/users/username/${encodeURIComponent(state.username)}`);
  setSessionUser(user);
  hydrateUserFields(user);
  return user;
}

async function loadUsers() {
  const users = await api('/users');
  showResult(el.usersResult, users);
  return users;
}

async function loadUserByUsername(username) {
  const user = await api(`/users/username/${encodeURIComponent(username)}`);
  showResult(el.usersResult, user);
  return user;
}

async function loadUserById(id) {
  const user = await api(`/users/${id}`);
  showResult(el.usersResult, user);
  return user;
}

async function saveUserUpdates(id, body) {
  const cleaned = Object.fromEntries(Object.entries(body).filter(([, value]) => value !== ''));
  const user = await api(`/users/${id}`, { method: 'PUT', body: cleaned });
  showResult(el.usersResult, user);
  return user;
}

async function deleteUser(id) {
  await api(`/users/${id}`, { method: 'DELETE' });
  showResult(el.usersResult, { status: 'deleted', userId: id });
}

async function createWallet(userId) {
  const wallet = await api(`/wallet/${userId}/create`, { method: 'POST' });
  showResult(el.walletResult, wallet);
  return wallet;
}

async function getWallet(userId) {
  const wallet = await api(`/wallet/${userId}`);
  showResult(el.walletResult, wallet);
  return wallet;
}

async function changeWallet(userId, amount, action) {
  const wallet = await api(`/wallet/${userId}/${action}`, { method: 'POST', body: { amount } });
  showResult(el.walletResult, wallet);
  return wallet;
}

async function loadTransactions(userId) {
  const txs = await api(`/wallet/${userId}/transactions`);
  showResult(el.walletResult, txs);
  return txs;
}

async function loadPortfolio(userId) {
  const portfolio = await api(`/portfolio/${userId}`);
  showResult(el.portfolioResult, portfolio);
  return portfolio;
}

async function loadHolding(userId, symbol) {
  const holding = await api(`/portfolio/${userId}/holding/${encodeURIComponent(symbol)}`);
  showResult(el.portfolioResult, holding);
  return holding;
}

async function tradeHolding(userId, payload, action) {
  const holding = await api(`/portfolio/${userId}/${action}`, { method: 'POST', body: payload });
  showResult(el.portfolioResult, holding);
  return holding;
}

async function executeTrade(action, payload) {
  const trade = await api(`/trades/${action}`, { method: 'POST', body: payload });
  showResult(el.tradesResult, trade);
  return trade;
}

async function listTrades(userId) {
  const trades = await api(`/trades/${userId}`);
  showResult(el.tradesResult, trades);
  return trades;
}

async function tradeDetail(id) {
  const trade = await api(`/trades/detail/${id}`);
  showResult(el.tradesResult, trade);
  return trade;
}

function readForm(form, fields) {
  return fields.reduce((acc, field) => {
    acc[field] = form.elements[field]?.value?.trim?.() ?? form.elements[field]?.value;
    return acc;
  }, {});
}

function activateTab(name) {
  document.querySelectorAll('.tab').forEach((button) => {
    button.classList.toggle('active', button.dataset.tab === name);
  });
  document.querySelectorAll('.panel').forEach((panel) => {
    panel.classList.toggle('active', panel.dataset.panel === name);
  });
}

document.querySelectorAll('.tab').forEach((button) => {
  button.addEventListener('click', () => activateTab(button.dataset.tab));
});

document.getElementById('pingBtn').addEventListener('click', async () => {
  try {
    await fetch(`${API_PREFIX}/actuator/health`, { headers: authHeaders() });
    el.connectionBadge.textContent = 'Gateway reachable';
    el.connectionBadge.className = 'badge badge-success';
    setMessage('info', 'Gateway responded to health check.');
  } catch (error) {
    el.connectionBadge.textContent = 'Gateway unreachable';
    el.connectionBadge.className = 'badge badge-error';
    setMessage('error', error.message);
  }
});

document.getElementById('clearBtn').addEventListener('click', () => {
  localStorage.removeItem('stonk_token');
  localStorage.removeItem('stonk_username');
  localStorage.removeItem('stonk_role');
  localStorage.removeItem('stonk_user_id');
  state.token = '';
  state.username = '';
  state.role = '';
  state.userId = '';
  state.sessionUser = null;
  syncSessionUI();
  setMessage('info', 'Session cleared.');
});

forms.register.addEventListener('submit', async (event) => {
  event.preventDefault();
  clearMessage();
  try {
    const body = readForm(forms.register, ['username', 'email', 'password', 'role']);
    if (!body.role) delete body.role;
    const result = await api('/auth/register', { method: 'POST', body });
    persistSession(result);
    el.authResult.textContent = JSON.stringify(result, null, 2);
    const user = await resolveCurrentUser();
    if (user) {
      setMessage('success', `Registered ${user.username} and loaded profile.`);
    } else {
      setMessage('success', `Registered ${result.username}.`);
    }
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.login.addEventListener('submit', async (event) => {
  event.preventDefault();
  clearMessage();
  try {
    const body = readForm(forms.login, ['username', 'password']);
    const result = await api('/auth/login', { method: 'POST', body });
    persistSession(result);
    el.authResult.textContent = JSON.stringify(result, null, 2);
    const user = await resolveCurrentUser();
    if (user) {
      setMessage('success', `Logged in as ${user.username}.`);
    } else {
      setMessage('success', `Logged in as ${result.username}.`);
    }
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.userByUsername.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { username } = readForm(forms.userByUsername, ['username']);
    await loadUserByUsername(username);
    setMessage('success', `Loaded user ${username}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.userById.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { id } = readForm(forms.userById, ['id']);
    await loadUserById(id);
    setMessage('success', `Loaded user ${id}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.updateUser.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { id, username, email } = readForm(forms.updateUser, ['id', 'username', 'email']);
    await saveUserUpdates(id, { username, email });
    setMessage('success', `Updated user ${id}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.deleteUser.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { id } = readForm(forms.deleteUser, ['id']);
    await deleteUser(id);
    setMessage('success', `Deleted user ${id}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

document.getElementById('loadUsersBtn').addEventListener('click', async () => {
  try {
    await loadUsers();
    setMessage('success', 'Loaded all users.');
  } catch (error) {
    setMessage('error', error.message);
  }
});

document.getElementById('useSessionUserBtn').addEventListener('click', () => {
  if (!state.userId) {
    setMessage('error', 'No current user ID available.');
    return;
  }
  hydrateUserFields({ id: state.userId });
  setMessage('info', `Filled fields with user ${state.userId}.`);
});

document.getElementById('walletUseSessionBtn').addEventListener('click', () => {
  if (!state.userId) {
    setMessage('error', 'No current user ID available.');
    return;
  }
  hydrateUserFields({ id: state.userId });
  setMessage('info', `Filled wallet fields with user ${state.userId}.`);
});

document.getElementById('portfolioUseSessionBtn').addEventListener('click', () => {
  if (!state.userId) {
    setMessage('error', 'No current user ID available.');
    return;
  }
  hydrateUserFields({ id: state.userId });
  setMessage('info', `Filled portfolio fields with user ${state.userId}.`);
});

forms.walletLookup.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { userId } = readForm(forms.walletLookup, ['userId']);
    await getWallet(userId);
    setMessage('success', `Loaded wallet for user ${userId}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.walletCreate.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { userId } = readForm(forms.walletCreate, ['userId']);
    await createWallet(userId);
    setMessage('success', `Created wallet for user ${userId}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.walletChange.addEventListener('submit', async (event) => {
  event.preventDefault();
  const action = event.submitter?.value;
  try {
    const { userId, amount } = readForm(forms.walletChange, ['userId', 'amount']);
    await changeWallet(userId, amount, action);
    setMessage('success', `${action} applied for user ${userId}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.walletTx.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { userId } = readForm(forms.walletTx, ['userId']);
    await loadTransactions(userId);
    setMessage('success', `Loaded transactions for user ${userId}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.portfolioLookup.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { userId } = readForm(forms.portfolioLookup, ['userId']);
    await loadPortfolio(userId);
    setMessage('success', `Loaded portfolio for user ${userId}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.holdingLookup.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { userId, symbol } = readForm(forms.holdingLookup, ['userId', 'symbol']);
    await loadHolding(userId, symbol);
    setMessage('success', `Loaded holding ${symbol} for user ${userId}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.portfolioTrade.addEventListener('submit', async (event) => {
  event.preventDefault();
  const action = event.submitter?.value;
  try {
    const { userId, symbol, quantity, price } = readForm(forms.portfolioTrade, ['userId', 'symbol', 'quantity', 'price']);
    await tradeHolding(userId, { symbol, quantity: Number(quantity), price: Number(price) }, action);
    setMessage('success', `${action} request submitted for ${symbol}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.tradeAction.addEventListener('submit', async (event) => {
  event.preventDefault();
  const action = event.submitter?.value;
  try {
    const { userId, symbol, quantity } = readForm(forms.tradeAction, ['userId', 'symbol', 'quantity']);
    await executeTrade(action, { userId: Number(userId), symbol, quantity: Number(quantity) });
    setMessage('success', `${action} trade submitted for ${symbol}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.tradeList.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { userId } = readForm(forms.tradeList, ['userId']);
    await listTrades(userId);
    setMessage('success', `Loaded trades for user ${userId}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

forms.tradeDetail.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const { id } = readForm(forms.tradeDetail, ['id']);
    await tradeDetail(id);
    setMessage('success', `Loaded trade ${id}.`);
  } catch (error) {
    setMessage('error', error.message);
  }
});

async function boot() {
  syncSessionUI();
  try {
    const health = await fetch(`${API_PREFIX}/actuator/health`).then((r) => r.json());
    el.connectionBadge.textContent = health.status === 'UP' ? 'Gateway healthy' : 'Gateway responding';
    el.connectionBadge.className = 'badge badge-success';
  } catch {
    el.connectionBadge.textContent = 'Gateway offline';
    el.connectionBadge.className = 'badge badge-error';
  }

  if (state.token && state.username) {
    try {
      const user = await resolveCurrentUser();
      if (!user && state.userId) {
        hydrateUserFields({ id: state.userId });
      }
    } catch (error) {
      setMessage('error', `Session restore failed: ${error.message}`);
    }
  }

  const seedUser = state.userId ? { id: state.userId } : state.sessionUser;
  if (seedUser?.id) {
    hydrateUserFields(seedUser);
  }
}

boot();
