const http = require('http');
const fs = require('fs');
const path = require('path');
const { URL } = require('url');

const PORT = Number(process.env.PORT || 4173);
const TARGET = process.env.API_TARGET || 'http://localhost:8080';
const ROOT = __dirname;

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml; charset=utf-8',
  '.png': 'image/png',
  '.ico': 'image/x-icon',
};

const server = http.createServer(async (req, res) => {
  try {
    const requestUrl = new URL(req.url, `http://${req.headers.host}`);

    if (requestUrl.pathname.startsWith('/api/')) {
      return proxy(req, res, requestUrl);
    }

    return serveStatic(req, res, requestUrl.pathname);
  } catch (error) {
    res.writeHead(500, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({ error: 'Server error', message: error.message }));
  }
});

server.listen(PORT, () => {
  console.log(`Stonk frontend running at http://localhost:${PORT}`);
  console.log(`Proxying API requests to ${TARGET}`);
});

function serveStatic(req, res, pathname) {
  const filePath = pathname === '/' ? path.join(ROOT, 'index.html') : path.join(ROOT, pathname);
  const normalized = path.normalize(filePath);

  if (!normalized.startsWith(ROOT)) {
    res.writeHead(403);
    res.end('Forbidden');
    return;
  }

  fs.readFile(normalized, (err, data) => {
    if (err) {
      res.writeHead(404);
      res.end('Not found');
      return;
    }

    const ext = path.extname(normalized).toLowerCase();
    res.writeHead(200, {
      'Content-Type': MIME[ext] || 'application/octet-stream',
      'Cache-Control': 'no-store',
    });
    res.end(data);
  });
}

function proxy(req, res, requestUrl) {
  const upstreamUrl = new URL(TARGET);
  upstreamUrl.pathname = requestUrl.pathname.replace(/^\/api/, '') || '/';
  upstreamUrl.search = requestUrl.search;

  const transport = upstreamUrl.protocol === 'https:' ? require('https') : require('http');
  const headers = { ...req.headers };
  delete headers.host;

  const upstreamReq = transport.request(
    upstreamUrl,
    {
      method: req.method,
      headers,
    },
    (upstreamRes) => {
      const responseHeaders = { ...upstreamRes.headers };
      responseHeaders['access-control-allow-origin'] = '*';
      responseHeaders['access-control-allow-headers'] = 'Authorization, Content-Type';
      responseHeaders['access-control-allow-methods'] = 'GET,POST,PUT,DELETE,OPTIONS';

      res.writeHead(upstreamRes.statusCode || 502, responseHeaders);
      upstreamRes.pipe(res);
    },
  );

  upstreamReq.on('error', (error) => {
    res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({ error: 'Bad Gateway', message: error.message }));
  });

  req.pipe(upstreamReq);
}
