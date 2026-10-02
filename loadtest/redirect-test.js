// k6 load test for the redirect path.
// Usage: create a short link first, then: k6 run -e CODE=<code> loadtest/redirect-test.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    redirects: {
      executor: 'constant-arrival-rate',
      rate: 10000,          // target requests per second
      timeUnit: '1s',
      duration: '60s',
      preAllocatedVUs: 200,
      maxVUs: 1000,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<100'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE}/${__ENV.CODE}`, { redirects: 0 });
  check(res, { 'is 302': (r) => r.status === 302 });
}
