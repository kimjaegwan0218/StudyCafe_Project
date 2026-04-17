// /assets/js/price.js
console.log("🔥 price.js loaded");

window.pricePage = function () {
    const API = {
        TIME_PRICES: "/api/time-prices",
        SEAT_SURCHARGES: "/api/seats/surcharges",     // ✅ 우리가 추가할 API
        SUB_PLANS: "/api/subscription-plans",
    };

    return {
        loading: false,
        error: "",

        timePlans: [],     // [{hours, price}]
        seatOptions: [],   // [{key, title, extra, desc}]
        subs: [],          // [{subscriptionType, days, price, title, desc, hot}]

        fmt(n) {
            return Number(n || 0).toLocaleString("ko-KR") + "원";
        },

        async init() {
            this.loading = true;
            this.error = "";

            try {
                await Promise.all([
                    this.loadTimePrices(),
                    this.loadSeatSurcharges(),
                    this.loadSubscriptionPlans(),
                ]);
            } catch (e) {
                console.error(e);
                this.error = e?.message || "요금 정보를 불러오지 못했습니다.";
            } finally {
                this.loading = false;
            }
        },

        // ---------------------
        // time_prices
        // ---------------------
        async loadTimePrices() {
            const data = await this.fetchJson(API.TIME_PRICES);

            // data: [{durationHours, price}, ...]
            this.timePlans = (data || [])
                .slice()
                .sort((a, b) => Number(a.durationHours) - Number(b.durationHours))
                .map(tp => ({
                    hours: Number(tp.durationHours),
                    price: Number(tp.price || 0),
                }));
        },

        // ---------------------
        // seats surcharge
        // ---------------------
        async loadSeatSurcharges() {
            const data = await this.fetchJson(API.SEAT_SURCHARGES);
            // data: [{type:"COUPLE", surchargePrice:1000}, {type:"MEETING", surchargePrice:2000}]
            const list = Array.isArray(data) ? data : [];

            const byType = new Map();
            for (const row of list) {
                const key = String(row.type || "").toUpperCase();
                const extra = Number(row.surchargePrice ?? row.surcharge_price ?? 0);
                if (!key) continue;
                byType.set(key, extra);
            }

            const couple = byType.get("COUPLE") ?? 0;
            const meeting = byType.get("MEETING") ?? 0;

            const opts = [];
            if (couple > 0) {
                opts.push({
                    key: "COUPLE",
                    title: "커플석 (빨강)",
                    extra: couple,
                    desc: `기본요금 + ${this.fmt(couple)}`,
                });
            }
            if (meeting > 0) {
                opts.push({
                    key: "MEETING",
                    title: "회의실 대여 (노랑)",
                    extra: meeting,
                    desc: `기본요금 + ${this.fmt(meeting)}`,
                });
            }

            this.seatOptions = opts;
        },

        // ---------------------
        // subscription_price
        // ---------------------
        async loadSubscriptionPlans() {
            const data = await this.fetchJson(API.SUB_PLANS);
            // data: [{subscriptionType, subType(durationDays), price, durationDays}, ...]
            this.subs = (data || [])
                .slice()
                .sort((a, b) => Number(a.subType) - Number(b.subType))
                .map(p => {
                    const days = Number(p.subType ?? p.durationDays ?? 0);
                    const type = String(p.subscriptionType || p.type || "");
                    const price = Number(p.price || 0);

                    return {
                        subscriptionType: type,
                        days,
                        price,
                        title: this.subTitle(days),
                        desc: this.subDesc(days),
                        hot: days === 30,
                    };
                });
        },

        subTitle(days) {
            if (days === 7) return "7일권";
            if (days === 30) return "30일 무제한";
            if (days === 365) return "연간 이용권";
            return `${days}일 플랜`;
        },

        subDesc(days) {
            if (days === 7) return "24시간 무제한";
            if (days === 30) return "24시간 무제한 / 음료 완전 무료";
            if (days === 365) return "월 환산 3.3만원 / 최저가";
            return "설명 준비중";
        },

        // ---------------------
        // fetch helper
        // ---------------------
        async fetchJson(url, options = {}) {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 8000);

            try {
                const resp = await fetch(url, {
                    credentials: "include",
                    signal: controller.signal,
                    ...options,
                });

                const ct = resp.headers.get("content-type") || "";
                const isJson = ct.includes("application/json");

                if (!resp.ok) {
                    if (isJson) {
                        const j = await resp.json().catch(() => null);
                        throw new Error(j?.message || `HTTP ${resp.status}`);
                    } else {
                        const t = await resp.text().catch(() => "");
                        throw new Error(t || `HTTP ${resp.status}`);
                    }
                }

                if (!isJson) return {};
                return await resp.json();
            } catch (e) {
                if (e?.name === "AbortError") throw new Error("API 시간 초과 (8초)");
                throw e;
            } finally {
                clearTimeout(timeoutId);
            }
        },
    };
};
