import { loadSession } from './authStorage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

async function parseResponse(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function buildHeaders(headers = {}) {
  const session = loadSession();
  const nextHeaders = {
    Accept: 'application/json',
    ...headers,
  };

  if (session?.token) {
    nextHeaders.Authorization = `Bearer ${session.token}`;
  }

  return nextHeaders;
}

async function request(path, options = {}) {
  const hasBody = options.body !== undefined && options.body !== null;
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers: buildHeaders({
      ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
      ...(options.headers || {}),
    }),
    body: hasBody ? JSON.stringify(options.body) : undefined,
  });

  const payload = await parseResponse(response);
  if (!response.ok) {
    const message = payload?.message || payload?.error || response.statusText || 'Request failed';
    const error = new Error(message);
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
}

export const apiClient = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  patch: (path, body) => request(path, { method: 'PATCH', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
  request,
  API_BASE_URL,
};
