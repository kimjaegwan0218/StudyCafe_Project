// /assets/js/booking.js
console.log("🔥 booking.js loaded");

window.bookingPage = function () {
    const API = {
        TIME_PRICES: "/api/time-prices",
        SEATS_AVAIL: "/api/seats/availability",          // ?date&time&hours
        CREATE_RES_PAYMENT: "/api/reservations/payment", // ✅ PaymentController 매핑

        // ✅ 구독 상태: 1순위(/me) → 2순위(/active) fallback
        SUB_ME: "/api/subscriptions/me",
        SUB_ACTIVE: "/api/subscriptions/active",
    };

    return {
        bookingStep: 1,

        selectedDate: "",
        selectedTime: "00:00",
        selectedPlan: null,

        selectedSeat: null,
        plans: [],
        seats: [],

        submitting: false,
        errorMsg: "",

        // ✅ 구독 여부 / 서버 응답 저장
        subscriptionActive: false,
        subscription: null,
        lastCreated: null,

        async init() {
            console.log("✅ booking init called");
            this.errorMsg = "";

            await this.loadPlans();

            if (!this.selectedPlan && this.plans.length > 0) {
                this.selectedPlan = this.plans[0];
            }

            await this.loadSubscriptionActive();
        },

        /* =======================
           UI helpers / price
           ======================= */
        getSeatExtraPrice() {
            return this.selectedSeat ? Number(this.selectedSeat.surchargePrice || 0) : 0;
        },

        previewBasePrice() {
            const baseFromServer =
                this.lastCreated?.basePrice ??
                this.lastCreated?.base ??
                this.lastCreated?.data?.basePrice ??
                this.lastCreated?.data?.base ??
                null;

            if (baseFromServer != null) return Number(baseFromServer);
            if (this.subscriptionActive) return 0;
            return this.selectedPlan ? Number(this.selectedPlan.price || 0) : 0;
        },

        previewTotalPrice() {
            return this.previewBasePrice() + this.getSeatExtraPrice();
        },

        getCoupleSurcharge() {
            const seat = (this.seats || []).find((s) => s.type === "couple");
            return seat ? Number(seat.surchargePrice || 0) : 0;
        },

        getMeetingSurcharge() {
            const seat = (this.seats || []).find((s) => s.type === "meeting");
            return seat ? Number(seat.surchargePrice || 0) : 0;
        },

        /* =======================
           Actions
           ======================= */
        selectPlan(plan) {
            this.selectedPlan = plan;
            this.selectedSeat = null;
            this.lastCreated = null;
        },

        selectSeat(seat) {
            if (!seat || seat.status !== "open") return;
            this.selectedSeat = seat;
        },

        async goToSeatSelect() {
            this.errorMsg = "";

            if (!this.selectedDate) return alert("이용일을 선택해 주세요.");
            if (!this.selectedPlan) return alert("이용 플랜을 선택해 주세요.");

            this.bookingStep = 2;
            await this.loadSeatsAvailability();
        },

        async submitReservation() {
            this.errorMsg = "";

            if (!this.selectedDate || !this.selectedTime || !this.selectedPlan || !this.selectedSeat) {
                alert("예약 정보가 부족합니다. 날짜/시간/플랜/좌석을 확인해 주세요.");
                return;
            }

            const userId = this.getUserId();
            if (!userId) {
                alert("로그인이 필요합니다. (userId 없음)");
                return;
            }

            if (this.submitting) return;
            this.submitting = true;

            try {
                const startAt = this.toIsoDateTime(this.selectedDate, this.selectedTime);

                // ✅ 구독자면 basePrice 0
                const basePriceForRequest = this.subscriptionActive ? 0 : Number(this.selectedPlan.price || 0);

                const payload = {
                    userId: Number(userId),
                    seatId: Number(this.selectedSeat.id),
                    startAt: startAt,
                    durationHours: Number(this.selectedPlan.hours),
                    basePrice: Number(basePriceForRequest),
                    surchargePrice: Number(this.selectedSeat.surchargePrice || 0),
                };

                console.log("➡️ POST /api/reservations/payment payload =", payload);

                const raw = await this.fetchJson(API.CREATE_RES_PAYMENT, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload),
                });

                console.log("✅ /api/reservations/payment raw =", raw);

                this.lastCreated = raw;

                if (!raw || raw.success === false) {
                    throw new Error(raw?.message || raw?.data?.message || "예약/결제 생성 실패");
                }

                const normalized = this.normalizeCreateResponse(raw);
                console.log("✅ normalized =", normalized);

                // ✅ 핵심: 서버 amount를 못 잡아도(=null) 프론트 계산값으로 확정
                const finalAmount = this.resolveFinalAmount(normalized);

                // ✅ 0원이면 결제 페이지로 가지 말고 바로 완료 처리
                if (finalAmount === 0) {
                    alert("0원 결제(무료)로 예약 완료!");
                    location.href = "/mypage";
                    return;
                }

                // 결제 페이지 이동 UI
                this.bookingStep = 4;

                if (!normalized.reservationId || !normalized.paymentId || !normalized.orderId) {
                    console.error("❌ 결제 이동 값 부족. raw=", raw);
                    throw new Error("결제 이동에 필요한 값이 부족합니다. (reservationId/paymentId/orderId)");
                }

                const qs = new URLSearchParams({
                    reservationId: String(normalized.reservationId),
                    paymentId: String(normalized.paymentId),
                    orderId: String(normalized.orderId),
                    amount: String(finalAmount), // ✅ finalAmount 사용
                    orderName: String(normalized.orderName || "StudyCafe 예약"),
                });

                location.href = "/booking/payment?" + qs.toString();
            } catch (e) {
                console.error(e);
                this.errorMsg = e?.message || "예약 처리 중 오류가 발생했습니다.";
                alert(this.errorMsg);
                this.bookingStep = 3;
            } finally {
                this.submitting = false;
            }
        },

        /* =======================
           API calls
           ======================= */
        async loadPlans() {
            try {
                const data = await this.fetchJson(API.TIME_PRICES);

                this.plans = (data || [])
                    .slice()
                    .sort((a, b) => Number(a.durationHours) - Number(b.durationHours))
                    .map((tp) => ({
                        name: `${tp.durationHours}시간`,
                        price: Number(tp.price || 0),
                        hours: Number(tp.durationHours),
                    }));

                console.log("✅ plans =", this.plans);
            } catch (e) {
                console.error(e);
                this.errorMsg = e?.message || "플랜 정보를 가져오지 못했습니다.";
                alert(this.errorMsg);
            }
        },

        async loadSubscriptionActive() {
            this.subscriptionActive = false;
            this.subscription = null;

            const endpoints = [API.SUB_ME, API.SUB_ACTIVE];

            for (const url of endpoints) {
                try {
                    const { ok, status, data } = await this.fetchJsonLenient(url);

                    if (!ok && status === 404) continue;

                    if (!ok && (status === 401 || status === 403)) {
                        console.warn(`subscription status blocked (${status}) at ${url}`);
                        this.subscriptionActive = false;
                        this.subscription = null;
                        return;
                    }

                    if (!ok) continue;

                    const active =
                        (typeof data === "boolean" ? data : null) ??
                        data?.active ??
                        data?.data?.active ??
                        data?.data ??
                        false;

                    this.subscriptionActive = !!active;
                    this.subscription = (typeof data === "object" && data) ? data : null;

                    console.log("✅ subscriptionActive =", this.subscriptionActive, "from=", url, "raw=", data);
                    return;
                } catch (e) {
                    console.warn("subscription check skipped:", url, e?.message || e);
                }
            }

            this.subscriptionActive = false;
            this.subscription = null;
        },

        async loadSeatsAvailability() {
            this.selectedSeat = null;

            try {
                const qs = new URLSearchParams({
                    date: this.selectedDate,
                    time: this.selectedTime,
                    hours: String(this.selectedPlan.hours),
                });

                const data = await this.fetchJson(`${API.SEATS_AVAIL}?${qs.toString()}`);
                console.log("✅ seats avail raw =", data);

                this.seats = (data.seats || [])
                    .slice()
                    .sort((a, b) => Number(a.seatId) - Number(b.seatId))
                    .map((s) => ({
                        id: Number(s.seatId),
                        status: s.status, // open | occupied
                        surchargePrice: Number(s.surchargePrice || 0),
                        type: s.type === "COUPLE" ? "couple" : s.type === "MEETING" ? "meeting" : "normal",
                    }));

                console.log("✅ seats =", this.seats);

                if (this.seats.length === 0) {
                    alert("좌석 데이터가 없습니다. seats 테이블/availability API를 확인해 주세요.");
                }
            } catch (e) {
                console.error(e);
                this.errorMsg = e?.message || "좌석 정보를 가져오지 못했습니다.";
                alert(this.errorMsg);
            }
        },

        /* =======================
           Utils
           ======================= */
        getUserId() {
            if (window.__USER_ID__) return window.__USER_ID__;

            const ls = localStorage.getItem("userId");
            if (ls) return Number(ls);

            return null;
        },

        toIsoDateTime(dateYYYYMMDD, hhmm) {
            const t = String(hhmm || "09:00");
            const hasSec = t.split(":").length === 3;
            return `${dateYYYYMMDD}T${hasSec ? t : (t + ":00")}`;
        },

        parseAmount(v) {
            if (v === null || v === undefined) return null;

            if (typeof v === "number") {
                return Number.isFinite(v) ? v : null;
            }

            if (typeof v === "string") {
                const digits = v.replace(/[^\d-]/g, "");
                if (!digits) return null;
                const n = Number(digits);
                return Number.isFinite(n) ? n : null;
            }

            return null;
        },

        normalizeCreateResponse(raw) {
            const src =
                raw?.data && typeof raw.data === "object"
                    ? raw.data
                    : raw;

            const amountRaw =
                src?.amount ??
                src?.totalAmount ??
                src?.finalAmount ??
                src?.finalPrice ??
                src?.final_price ??
                src?.totalPrice ??
                src?.total_price ??
                src?.payAmount ??
                src?.paymentAmount ??
                src?.payment_amount ??
                src?.payment?.amount ??
                src?.payment?.totalPrice ??
                null;

            return {
                success: raw?.success ?? src?.success,
                reservationId: src?.reservationId ?? src?.reservation_id ?? src?.id ?? null,
                paymentId: src?.paymentId ?? src?.payment_id ?? src?.payment?.id ?? null,
                orderId: src?.orderId ?? src?.order_id ?? src?.payment?.orderId ?? null,
                amount: this.parseAmount(amountRaw),
                orderName: src?.orderName ?? src?.order_name ?? src?.payment?.orderName ?? null,
                message: raw?.message ?? src?.message ?? null,
            };
        },

        // ✅ 최종 금액 확정:
        // 1) 서버에서 amount 파싱 성공 → 사용
        // 2) 못 받으면(=null) 프론트 계산(previewTotalPrice)로 확정
        resolveFinalAmount(normalized) {
            const a = normalized?.amount;
            if (a === 0) return 0;
            if (typeof a === "number" && Number.isFinite(a)) return a;

            // 서버가 amount를 안 주거나 키가 전혀 다르면 여기로 옴
            const fallback = this.previewTotalPrice(); // base(구독이면 0) + surcharge
            const n = this.parseAmount(fallback);
            return (typeof n === "number" && Number.isFinite(n)) ? n : 0;
        },

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
                        if (j?.message) throw new Error(j.message);
                        throw new Error(j ? JSON.stringify(j) : `HTTP ${resp.status}`);
                    } else {
                        const bodyText = await resp.text().catch(() => "");
                        throw new Error(bodyText || `HTTP ${resp.status}`);
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

        async fetchJsonLenient(url, options = {}) {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 8000);

            try {
                const resp = await fetch(url, {
                    credentials: "include",
                    signal: controller.signal,
                    ...options,
                });

                const status = resp.status;
                const ct = resp.headers.get("content-type") || "";
                const isJson = ct.includes("application/json");

                let data = null;
                if (isJson) data = await resp.json().catch(() => null);
                else data = await resp.text().catch(() => null);

                return { ok: resp.ok, status, data };
            } catch (e) {
                if (e?.name === "AbortError") return { ok: false, status: 0, data: { message: "API 시간 초과 (8초)" } };
                return { ok: false, status: 0, data: { message: e?.message || String(e) } };
            } finally {
                clearTimeout(timeoutId);
            }
        },
    };
};
