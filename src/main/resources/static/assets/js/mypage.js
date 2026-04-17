// /assets/js/mypage.js
console.log("🔥 mypage.js loaded");

window.mypage = function () {
    return {
        view: 'dashboard',
        historyModal: false,

        // ✅ 구독
        subscription: null,
        subscriptionLoaded: false,

        // ✅ 예약내역
        reservations: [],
        reservationsLoaded: false,

        inquiries: [],
        selectedInquiry: null,

        openInquiry(q) {
            this.selectedInquiry = q;
            this.view = 'inquiry_detail';
        },

        saveProfile() {
            this.view = 'dashboard';
        },

        async init() {
            await Promise.all([
                this.loadSubscription(),
                this.loadReservations()
            ]);
        },

        // =========================
        // ✅ Subscription
        // =========================
        async loadSubscription() {
            this.subscriptionLoaded = false;

            // ✅ booking 쪽에서 살아있는 엔드포인트로 맞춤
            const candidates = [
                '/api/subscription/me',
                '/api/subscriptions/me',
            ];

            try {
                let data = null;
                let lastErr = null;

                // 후보 URL 중 성공하는 것 사용
                for (const url of candidates) {
                    try {
                        const res = await fetch(url, { credentials: 'include' });
                        if (!res.ok) {
                            lastErr = new Error(`HTTP ${res.status} @ ${url}`);
                            continue;
                        }

                        const ct = res.headers.get('content-type') || '';
                        if (!ct.includes('application/json')) {
                            lastErr = new Error(`Not JSON @ ${url}`);
                            continue;
                        }

                        data = await res.json();
                        // 여기까지 왔으면 일단 이 URL은 성공
                        break;
                    } catch (e) {
                        lastErr = e;
                    }
                }

                if (!data) {
                    console.warn('[subscription] all endpoints failed:', lastErr?.message || lastErr);
                    this.subscription = null;
                    return;
                }

                // ✅ 서버 응답에서 active를 널널하게 읽기
                const active = !!(data.active ?? data?.data?.active ?? false);
                if (!active) {
                    this.subscription = null;
                    return;
                }

                // ✅ endAt 파싱
                const endAtRaw = data.endAt ?? data.end_at ?? null;
                const startAtRaw = data.startAt ?? data.start_at ?? null;

                const endAt = endAtRaw ? new Date(endAtRaw) : null;
                const startAt = startAtRaw ? new Date(startAtRaw) : null;

                // ✅ remainingDays 계산 (endAt이 없으면 0으로 처리)
                const remainingDays = endAt
                    ? Math.max(0, Math.ceil((endAt.getTime() - Date.now()) / 86400000))
                    : 0;

                // ✅ planName 만들기 (subscriptionType 기준)
                const subType = String(data.subscriptionType ?? data.type ?? '').toUpperCase();
                const planName = this.planNameFromType(subType, remainingDays);

                // ✅ 화면이 기대하는 형태로 저장
                this.subscription = {
                    active: true,
                    code: data.code ?? null,
                    subscriptionType: subType,
                    planName,
                    remainingDays,
                    startAt: startAtRaw,
                    endAt: endAtRaw,
                };

                console.log("✅ subscription mapped:", this.subscription);

            } catch (e) {
                console.error('[subscription]', e);
                this.subscription = null;
            } finally {
                this.subscriptionLoaded = true;
            }
        },

        planNameFromType(type, days) {
            // 서버 타입: WEEK_7 / MONTH_30 / YEAR_365
            if (type === 'WEEK_7') return '7일권';
            if (type === 'MONTH_30') return '30일 무제한';
            if (type === 'YEAR_365') return '연간 이용권';

            // 타입이 없거나 예상 외면 남은 일수 기반 fallback
            if (days >= 300) return '연간 이용권';
            if (days >= 25) return '30일 무제한';
            if (days >= 5) return '7일권';
            return '구독 이용권';
        },

        // =========================
        // Reservations
        // =========================
        async loadReservations() {
            try {
                const res = await fetch('/api/reservations/me', { credentials: 'include' });

                if (!res.ok) {
                    console.warn('[reservations] not ok:', res.status);
                    this.reservations = [];
                    return;
                }

                const data = await res.json();
                this.reservations = Array.isArray(data) ? data : [];
            } catch (e) {
                console.error('[reservations]', e);
                this.reservations = [];
            } finally {
                this.reservationsLoaded = true;
            }
        }
    }
}
