import http from 'k6/http';
import { check } from 'k6';

// 1. The Configuration (1000 people clicking continuously for 10 seconds)
export const options = {
    vus: 1000,
    duration: '10s',
};

// 2. The Execution (What each user does)
export default function () {
    const url = 'http://localhost:8080/api/tickets/buy';

    const payload = JSON.stringify({
        userId: Math.floor(Math.random() * 1000) + 1, // Random user ID
        eventId: 1,
        quantity: 2,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 3. Fire the request
    const res = http.post(url, payload, params);

    // 4. Verify it was successful (200 OK or 400 Sold Out)
    check(res, {
        'status is 200 or 400': (r) => r.status === 200 || r.status === 400,
    });
}
