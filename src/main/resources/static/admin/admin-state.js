window.adminApp = () => ({
    // 공통
    subpage: "",
    selectedPost: null,
    visiblePosts: 10,

    // ✅ 관리자 전용: 항상 관리자 모드로 고정
    isAdmin: true,


    remainingDays: 12,

    usersList: [],


    // 관리자 대시보드
    adminTab: "calendar",
    adminSubpage: "",
    selectedAdminDate: 7,
    selectedUser: {},

    userPage: {
        page: 1,
        size: 10,
        totalPages: 1,
        totalElements: 0
    },

    getUserActive(u) {
        // DTO가 userActive로 오든 active로 오든 둘 다 커버
        if (!u) return false;
        if (typeof u.userActive === "boolean") return u.userActive;
        if (typeof u.active === "boolean") return u.active;
        return false;
    },

    // users 관련 상태
    isUsersLoading: false,
    usersErrorMessage: "",

    async loadUsers() {
        this.isUsersLoading = true;
        this.usersErrorMessage = "";

        try {
            const page = this.userPage?.page || 1;
            const size = this.userPage?.size || 10;

            const page0 = Math.max(0, (this.userPage?.page || 1) - 1);
            const res = await axios.get("/api/admin/users", { params: { page: page0, size } });

            console.log("USERS raw:", res.data);
            console.log("USERS keys:", Object.keys(res.data || {}));

            const data = res.data || {};
            const list = data.content || data.dtoList || [];

            this.usersList = list.map(u => ({
                id: u.userId,
                name: u.name,
                email: u.email,
                phone: u.phone,
                active: u.active,
                role: u.role,
                isFrozen: !u.active
            }));

            this.userPage.totalPages = data.totalPages ?? data.totalPage ?? 1;
            this.userPage.totalElements = data.totalElements ?? data.totalCount ?? 0;

            if (typeof data.number === "number") this.userPage.page = data.number + 1;

        } catch (e) {
            this.usersErrorMessage = e?.response?.data?.message || "회원 목록 조회 실패";
            console.error(e);
        } finally {
            this.isUsersLoading = false;
        }
    },

    async searchUsers() {
        const keyword = (this.userSearchQuery || "").trim();

        // 검색어 없으면 전체 로딩
        if (keyword === "") {
            await this.loadUsers();
            return;
        }

        this.isUsersLoading = true;
        this.usersErrorMessage = "";

        try {
            const page = this.userPage?.page || 1;
            const size = this.userPage?.size || 10;

            const page0 = Math.max(0, (this.userPage?.page || 1) - 1);
            const res = await axios.get("/api/admin/users/search", {
                params: { keyword, page: page0, size }
            });

            const data = res.data || {};
            const list = data.content || data.dtoList || [];

            this.usersList = list.map(u => ({
                id: u.userId,
                name: u.name,
                email: u.email,
                phone: u.phone,
                active: u.active,
                role: u.role,
                isFrozen: !u.active
            }));

            this.userPage.totalPages = data.totalPages ?? data.totalPage ?? 1;
            this.userPage.totalElements = data.totalElements ?? data.totalCount ?? 0;
            if (typeof data.number === "number") this.userPage.page = data.number + 1;

        } catch (e) {
            const msg = e?.response?.data?.message || "회원 검색 실패";
            this.usersErrorMessage = msg;
            console.error(e);
        } finally {
            this.isUsersLoading = false;
        }
    },


    // ✅ 총 회원 수(전체) : 현재 페이지 길이가 아니라 전체 totalElements로
    get totalUserCount() {
        return this.userPage?.totalElements ?? (this.usersList?.length ?? 0);
    },

    get unansweredInquiryCount() {
        return (this.inquiries || []).filter(i =>
            i.status === 'CLOSED' || i.status === 'OPEN'
        ).length;
    },
    todayReservationCount: 0,

    async init() {
        await this.loadUsers();
        this.inquiryStatus = 'ALL';
        await this.fetchInquiries();

        this.initCalendar(); // ✅ 추가
    },

    async openUserDetail(userId) {
        this.usersErrorMessage = "";
        try {
            // ✅ DTO 조회 (AdminUserDetailDTO)
            const res = await axios.get(`/api/admin/users/detail/${userId}`);
            this.selectedUser = res.data;         // { userId, name, email, phone, userActive, subscriptionActive }
            this.adminSubpage = "user_detail";    // 모달 열기
        } catch (e) {
            // 404면 "User not found" 같은 메시지 받을 거고
            // 그 외는 서버 오류일 수도 있음
            const msg = e?.response?.data?.message || "회원 상세 조회 실패";
            this.usersErrorMessage = msg;
            alert(msg);
        }
    },

    get userPageNumbers() {
        const total = this.userPage.totalPages || 1;
        const cur = this.userPage.page || 1;
        let start = Math.max(1, cur - 2);
        let end = Math.min(total, start + 4);
        start = Math.max(1, end - 4);

        const arr = [];
        for (let i = start; i <= end; i++) arr.push(i);
        return arr;
    },

    goUserPage(p) {
        if (!p) return;
        this.userPage.page = p;

        const keyword = (this.userSearchQuery || "").trim();
        if (keyword !== "") {
            this.searchUsers();
        } else {
            this.loadUsers();
        }
    },


    // ✅ 화면에 표시할 회원 목록 (검색 + 필터)
    get filteredUsers() {
        const q = (this.userSearchQuery || "").trim().toLowerCase(); // ✅ 추가!

        return (this.usersList || []).filter((u) => {
            const name = (u.name || "").toLowerCase();
            const email = (u.email || "").toLowerCase();
            const matchesSearch = (q === "") || name.includes(q) || email.includes(q);

            const st = this.userFilterStatus || "all"; // all | frozen | active
            const matchesStatus =
                st === "all" ||
                (st === "frozen" && u.isFrozen) ||
                (st === "active" && !u.isFrozen);

            return matchesSearch && matchesStatus;
        });
    },

    // 예약 상세 모달 열기
    showUserDetail(data) {
        this.selectedUser = data;
        this.adminSubpage = "user_detail";
    },

    selectedDateStr: null,

    // ✅ 좌석 18개 고정
    seatList: Array.from({ length: 18 }, (_, i) => i + 1),

    // ✅ 선택한 날짜의 예약들(막대용 raw)
    dayReservations: [],

    reservationReqNo: 0,

    // "HH:mm" -> 분(0~1440)
    timeToMin(t) {
        if (!t) return 0;
        const [hh, mm] = t.split(":").map(Number);
        return (hh * 60) + (mm || 0);
    },

    // 분 -> 10분 슬롯 index (0~143)
    minToSlot10(min) {
        return Math.floor(min / 10);
    },

    // ✅ 예약 막대 스타일(absolute left/width %)
    barStyle(r) {
        const startMin = this.timeToMin(r.start);
        const endMin = this.timeToMin(r.end);

        const startSlot = this.minToSlot10(startMin);
        const endSlotRaw = this.minToSlot10(endMin);
        const endSlot = Math.min(144, endSlotRaw); // end는 144까지 허용하되,

        const totalSlots = 144;
        const left = (startSlot / totalSlots) * 100;
        const width = Math.max(1, ((endSlot - startSlot) / 144) * 100);

        return `left:${left}%; width:${width}%;`;
    },

    getSeatReservations(seat) {
        return (this.dayReservations || []).filter(r => r.seatId === seat);
    },

    async loadReservationsByDate() {
        // ✅ 요청 번호 증가 (가장 마지막 요청만 유효)
        const reqNo = ++this.reservationReqNo;

        // selectedDateStr 없으면 안전 처리
        if (!this.selectedDateStr) {
            this.dayReservations = [];
            this.todayReservationCount = 0;
            return;
        }

        try {
            const res = await axios.get("/api/admin/reservations/day", {
                params: { date: this.selectedDateStr }
            });

            // ✅ 이 응답이 "최신 요청"이 아니면 버림 (덮어쓰기 방지)
            if (reqNo !== this.reservationReqNo) return;

            const list = res.data || [];

            // startAt: "2026-01-17T10:00:00" -> "10:00"
            const toHHmm = (iso) => {
                if (!iso) return "00:00";
                const t = String(iso).split("T")[1] || "";
                return (t.substring(0, 5) || "00:00");
            };

            // "10:00" + minutes -> "12:30"
            const addMinutes = (hhmm, minutes) => {
                const [hh, mm] = hhmm.split(":").map(Number);
                const total = (hh * 60) + (mm || 0) + (Number(minutes) || 0);
                const endH = Math.floor(total / 60);
                const endM = total % 60;
                return `${String(endH).padStart(2, "0")}:${String(endM).padStart(2, "0")}`;
            };

            this.dayReservations = list.map(dto => {
                const start = toHHmm(dto.startAt);

                // ✅ durationMinutes 컬럼/필드 없으니 durationHours(시간) -> 분으로 변환
                const hours = Number(dto.durationHours ?? dto.duration_hours ?? 0);
                const end = addMinutes(start, hours * 60);

                return {
                    reservationId: dto.reservationId,
                    seatId: dto.seatId,
                    name: dto.userName,
                    start,
                    end,
                    status: dto.status
                };
            });

            this.todayReservationCount = this.dayReservations.length;

        } catch (e) {
            // ✅ 에러도 최신 요청일 때만 반영 (옛날 요청 에러가 화면 날리는 거 방지)
            if (reqNo !== this.reservationReqNo) return;

            console.error(e);
            this.dayReservations = [];
            this.todayReservationCount = 0;
        }
    },


    initCalendar() {
        const el = document.getElementById('fcCalendar');
        if (!el) return;

        const cal = new FullCalendar.Calendar(el, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            height: 'auto',
            dateClick: async (info) => {
                this.selectedDateStr = info.dateStr;
                await this.loadReservationsByDate();
            }
        });

        cal.render();
        this._calendar = cal; // 필요하면 저장
    },

    // 예약
    bookingStep: 1,
    selectedDate: "",
    selectedTime: "09:00",

    // ✅ 중요: 플랜/상태 같은 값은 영어로 통일 (백엔드랑 맞추기 쉬움)
    selectedPlan: { name: "1시간 패키지", price: 1000, hours: 1 },

    selectedSeat: null,
    redeemCode: "",
    isExtraFeeWaived: false,
    couponCode: "",
    subscriptionCodeInput: "",
    isSubscriptionApplied: false,
    mySubscriptionCode: "SUB-PRM-7788",
    showSubCode: false,

    seats: [
        { id: 1, type: "normal", status: "occupied", remaining: "45분" },
        { id: 2, type: "normal", status: "open", remaining: null },
        { id: 3, type: "normal", status: "occupied", remaining: "12분" },
        { id: 4, type: "normal", status: "open", remaining: null },
        { id: 5, type: "normal", status: "open", remaining: null },
        { id: 6, type: "normal", status: "occupied", remaining: "110분" },
        { id: 7, type: "normal", status: "open", remaining: null },
        { id: 8, type: "couple", status: "open", remaining: null },
        { id: 9, type: "couple", status: "open", remaining: null },
        { id: 10, type: "normal", status: "open", remaining: null },
        { id: 11, type: "normal", status: "open", remaining: null },
        { id: 12, type: "normal", status: "occupied", remaining: "5분" },
        { id: 13, type: "normal", status: "open", remaining: null },
        { id: 14, type: "normal", status: "open", remaining: null },
        { id: 15, type: "normal", status: "open", remaining: null },
        { id: 16, type: "normal", status: "open", remaining: null },
        { id: 17, type: "normal", status: "open", remaining: null },
        { id: 18, type: "meeting", status: "open", remaining: null },
    ],

    validRedeemCodes: ["FREE100", "SPECIAL2024"],

    // 구독 코드 확인
    checkSubCode() {
        if (this.subscriptionCodeInput === this.mySubscriptionCode) {
            this.isSubscriptionApplied = true;
            this.selectedPlan = { name: "구독 이용", price: 0, hours: 24 };
            alert("구독 코드가 확인되었습니다. 플랜 선택이 필요 없습니다.");
        } else {
            alert("유효하지 않은 구독 코드입니다.");
            this.isSubscriptionApplied = false;
        }
    },

    // 예약 종료 시간 계산
    getEndTime() {
        if (!this.selectedTime) return "";
        let [h, m] = this.selectedTime.split(":").map(Number);
        let endH = h + this.selectedPlan.hours;
        return `${String(endH).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
    },

    // 좌석 추가요금
    getSeatExtraPrice() {
        if (this.isExtraFeeWaived) return 0;
        if (this.selectedSeat?.type === "couple") return 1000;
        if (this.selectedSeat?.type === "meeting") return 2000;
        return 0;
    },

    // 총 금액 계산
    getTotalPrice() {
        let basePrice = this.isSubscriptionApplied ? 0 : this.selectedPlan.price;
        let total = basePrice + this.getSeatExtraPrice();
        if (this.couponCode === "DISCOUNT10") total -= 500;
        return Math.max(0, total);
    },


    // 공지(뉴스) - DB 연동
    newsCategory: "",
    selectedNews: null,
    newsItems: [],
    newsForm: { id: null, title: "", content: "", cat: "normal" },

    isNewsLoading: false,
    newsErrorMessage: "",

    async loadNews() {
        this.isNewsLoading = true;
        this.newsErrorMessage = "";
        try {
            const res = await axios.get("/api/admin/notices", {
                params: { cat: this.newsCategory }
            });
            this.newsItems = res.data || [];
            console.log("[NEWS] loaded:", this.newsItems);
        } catch (e) {
            console.error(e);
            this.newsItems = [];
            this.newsErrorMessage = e?.response?.data?.message || `공지 목록 조회 실패 (${e?.response?.status || "?"})`;
        } finally {
            this.isNewsLoading = false;
        }
    },

    async deleteNews(id) {
        if (!confirm("이 공지를 삭제하시겠습니까?")) return;
        try {
            await axios.delete(`/api/admin/notices/${id}`);
            await this.loadNews();
        } catch (e) {
            console.error(e);
            alert(e?.response?.data?.message || "공지 삭제 실패");
        }
    },

    editNews(item) {
        this.newsForm = { id: item.id, title: item.title, content: item.content, cat: item.cat };
        this.subpage = "news_form";
    },

    async saveNews() {
        const payload = {
            cat: this.newsForm.cat,
            title: (this.newsForm.title || "").trim(),
            content: (this.newsForm.content || "").trim(),
        };

        if (!payload.title || !payload.content) {
            alert("제목/내용을 입력하세요.");
            return;
        }

        try {
            if (this.newsForm.id) {
                await axios.put(`/api/admin/notices/${this.newsForm.id}`, payload);
            } else {
                await axios.post("/api/admin/notices", payload);
            }

            await this.loadNews();
            this.subpage = "news_list";
            this.newsForm = { id: null, title: "", content: "", cat: this.newsCategory || "normal" };
        } catch (e) {
            console.error(e);
            alert(e?.response?.data?.message || "공지 저장 실패");
        }
    },


    // 커뮤니티 게시글(더미)
    posts: Array.from({ length: 30 }, (_, i) => ({
        id: i + 1,
        title: `공부 기록 #${i + 1}: ${["세무사", "영어", "IT 자격증", "웹디자인"][i % 4]} 학습`,
        author: `사용자${String.fromCharCode(65 + (i % 26))}`,
        date: "2024.05.21",
        tags: ["상담", "수학"],
        likes: i === 0 ? 150 : (i === 1 ? 120 : (i === 2 ? 105 : Math.floor(Math.random() * 90))),
        comments: [
            { user: "StudyCafe 운영", text: "‘차트식’ 입문서부터 시작하는 걸 추천합니다!", date: "2024.03.21", role: "admin" },
            { user: "일반 사용자", text: "저도 그 방법으로 공부 중이에요!", date: "2024.03.22", role: "user" },
        ],
        content: "집중할 수 있는 환경 덕분에 예상보다 빨리 참고서를 끝냈습니다. 커피가 맛있어요.",
    })),

    togglePin(post) {
        post.isPinned = !post.isPinned;
        if (post.isPinned) {
            this.posts = [post, ...this.posts.filter((p) => p.id !== post.id)];
            alert("게시글을 상단 고정했습니다.");
        } else {
            alert("상단 고정을 해제했습니다.");
        }
    },

    deletePost(postId) {
        if (confirm("이 게시글을 삭제하시겠습니까?")) {
            this.posts = this.posts.filter((p) => p.id !== postId);
            this.selectedPost = null;
            this.subpage = "";
        }
    },

    newCommentBody: "",
    addComment(post) {
        if (this.newCommentBody.trim() === "") return;
        if (!post.comments) post.comments = [];
        post.comments.push({
            user: this.user.name,
            text: this.newCommentBody,
            date: new Date().toLocaleDateString(),
            role: "user",
        });
        this.newCommentBody = "";
        alert("댓글이 등록되었습니다!");
    },

    // 로그인 (관리자 페이지에서도 데모용)
    isLoggedIn: false,
    loginEmail: "",
    loginPassword: "",
    login() {
        if (this.loginEmail && this.loginPassword) {
            this.isLoggedIn = true;
            alert("로그인 성공!");
            location.href = "/admin/mypage";
        } else {
            alert("이메일과 비밀번호를 입력해 주세요.");
        }
    },

    logout() {
        this.isLoggedIn = false;
        alert("로그아웃되었습니다.");
        location.href = "/admin/login";
    },

    // ✅ inquiry 상태값/검색값
    inquiryUserId: null,
    inquiryStatus: 'OPEN',
    inquiries: [],

    // ✅ 서버에 status 저장이 안 되는 경우를 대비한 "로컬 상태 오버라이드"
    // key: inquiryId, value: 'CLOSED' | 'OPEN' | 'ANSWERED'
    inquiryStatusOverrides: {},

    manualInquiryStatus: "OPEN",

    async fetchInquiries() {
        try {
            const userId = this.inquiryUserId;
            const st = this.inquiryStatus;

            const page1 = this.inquiryPage.page || 1;   // ✅ 1-base 그대로 보냄
            const size = this.inquiryPage.size || 10;

            const params = { page: page1, size };
            let url = "";

            if (st === "ALL") {
                url = userId
                    ? `/api/admin/users/${userId}/inquiries/all`
                    : `/api/admin/users/inquiries/all`;
            } else {
                url = userId
                    ? `/api/admin/users/${userId}/inquiries`
                    : `/api/admin/users/inquiries`;
                params.status = st;
            }

            console.log("[INQ] click page(1-base)=", this.inquiryPage.page, "-> request page0=", params.page);

            const res = await axios.get(url, { params });

            console.log("[INQ] response number(0-base)=", res.data?.number, " totalPages=", res.data?.totalPages);  // ✅ res로 받기
            const data = res.data || {};                    // ✅ res.data 사용

            const list = data.content || data.dtoList || [];
            this.inquiries = list;

            // ✅ 추가: 서버 목록을 받은 후, 로컬 오버라이드 다시 적용
            this.applyInquiryStatusOverrides();

            this.inquiryPage.totalPages = data.totalPages ?? data.totalPage ?? 1;
            this.inquiryPage.totalElements = data.totalElements ?? data.totalCount ?? 0;

            if (typeof data.number === "number") this.inquiryPage.page = data.number + 1;

        } catch (e) {
            console.error(e);
            this.inquiries = [];
            this.inquiryPage.totalPages = 1;
            this.inquiryPage.totalElements = 0;
        }
    },

    applyInquiryStatusOverrides() {
        const map = this.inquiryStatusOverrides || {};
        (this.inquiries || []).forEach(inq => {
            const v = map[inq.inquiryId];
            if (v) inq.status = v;
        });
    },

    // ✅ 답변 존재 여부(목록 DTO가 replyId를 주면 그걸 쓰고, 없으면 status로 fallback)
    hasReply(inq) {
        if (!inq) return false;
        if (inq.replyId) return true;              // 목록 DTO에 replyId가 있으면 최고
        if (inq.status === "ANSWERED") return true; // replyId가 없을 수도 있으니 status도 커버
        return false;
    },

    // ✅ 목록 버튼 텍스트: 답변 있으면 상세보기 / 없으면 답변/작성
    getInquiryActionLabel(inq) {
        return this.hasReply(inq) ? "상세보기" : "답변/작성";
    },

    async setInquiryStatus(inquiryId, newStatus) {
        // 1) 로컬 즉시 반영
        const row = (this.inquiries || []).find(x => x.inquiryId === inquiryId);
        if (row) row.status = newStatus;

        if (this.selectedInquiry?.inquiryId === inquiryId) {
            this.selectedInquiry.status = newStatus;
        }
        if (this.inquiryDetail?.inquiryId === inquiryId) {
            this.inquiryDetail.status = newStatus;
        }

        // 2) 서버 반영 시도
        try {
            await axios.put(`/api/admin/users/inquiries/${inquiryId}/status`, null, {
                params: { status: newStatus }
            });

            // ✅ 서버 저장 성공이면 로컬 오버라이드 제거
            if (this.inquiryStatusOverrides) delete this.inquiryStatusOverrides[inquiryId];

        } catch (e) {
            // ✅ 서버 저장 실패면 "오버라이드 맵"에 저장해서 이후 fetch에도 유지
            if (!this.inquiryStatusOverrides) this.inquiryStatusOverrides = {};
            this.inquiryStatusOverrides[inquiryId] = newStatus;

            console.warn("status update API not available. keep local override.", e?.response?.status);
        }
    },

    async openInquiryReply(inq) {
        try {
            this.selectedInquiry = inq;

            // ✅ 모달 열기 = 열람 처리
            if (this.selectedInquiry?.status === "CLOSED") {
                await this.setInquiryStatus(this.selectedInquiry.inquiryId, "OPEN"); // 미열람 -> 미답변(열람)
            }

            this.manualInquiryStatus = this.selectedInquiry?.status || "OPEN";

            const res = await axios.get(`/api/admin/users/inquiries/detail/${inq.inquiryId}`);
            this.inquiryDetail = res.data;
            this.manualInquiryStatus = this.selectedInquiry?.status || "OPEN";

            // ✅ detail에 status가 있으면 우선 적용 (서버가 정답)
            if (this.inquiryDetail?.status) {
                await this.setInquiryStatus(inq.inquiryId, this.inquiryDetail.status);
            }

            // ✅ detail 기준으로 reply 존재 판단
            if (this.inquiryDetail?.replyId) {
                this.replyText = this.inquiryDetail.replyContent || "";
                this.isReplyReadonly = true;

                // ✅ 답변 있으면 상태는 무조건 ANSWERED로
                await this.setInquiryStatus(inq.inquiryId, "ANSWERED");
            } else {
                this.replyText = "";
                this.isReplyReadonly = false;

                // ✅ 답변 없으면 OPEN 유지(열람했으니)
                if (this.selectedInquiry?.status === "CLOSED") {
                    await this.setInquiryStatus(inq.inquiryId, "OPEN");
                }
            }

            this.replyErrorMessage = "";
            this.adminSubpage = "inquiry_reply";

        } catch (e) {
            console.error(e);
            alert("문의 상세 조회 실패");
        }
    },

    async clickUpdate() {
        // reply 없으면 수정 불가
        if (!this.inquiryDetail?.replyId) return;

        // 1) 읽기모드 -> 편집모드 전환만
        if (this.isReplyReadonly) {
            this.isReplyReadonly = false;
            return;
        }

        // 2) 편집모드 -> PUT 저장
        await this.updateReply();
    },

    async insertReply() {
        if (!this.selectedInquiry?.inquiryId) return;

        const content = (this.replyText || "").trim();
        if (content === "") {
            alert("답변 내용을 입력하세요.");
            return;
        }

        // 이미 답변 있으면 등록 금지
        if (this.inquiryDetail?.replyId) return;

        this.isReplySaving = true;
        this.replyErrorMessage = "";

        try {
            await axios.post(
                `/api/admin/inquiries/${this.selectedInquiry.inquiryId}/reply`,
                null,
                { params: { content } ,withCredentials: true }
            );

            // ✅ 최신 detail 재조회 + 목록 갱신
            await this.refreshInquiryDetailAndList();
            await this.setInquiryStatus(this.selectedInquiry.inquiryId, "ANSWERED");

            // 답변 생겼으니 읽기모드
            this.isReplyReadonly = true;

            alert("답변이 등록되었습니다.");
        } catch (e) {
            console.error(e);
            const msg = e?.response?.data?.message || "답변 등록 실패";
            this.replyErrorMessage = msg;
            alert(msg);
        } finally {
            this.isReplySaving = false;
        }
    },

    async updateReply() {
        if (!this.selectedInquiry?.inquiryId) return;

        const content = (this.replyText || "").trim();
        if (content === "") {
            alert("답변 내용을 입력하세요.");
            return;
        }

        // reply 없는데 update하면 의미 없음
        if (!this.inquiryDetail?.replyId) return;

        this.isReplySaving = true;
        this.replyErrorMessage = "";

        try {
            await axios.put(
                `/api/admin/inquiries/${this.selectedInquiry.inquiryId}/reply`,
                null,
                { params: { content } }
            );

            await this.refreshInquiryDetailAndList();
            await this.setInquiryStatus(this.selectedInquiry.inquiryId, "ANSWERED");

            // 저장 후 다시 읽기모드
            this.isReplyReadonly = true;

            alert("답변이 수정되었습니다.");
        } catch (e) {
            console.error(e);
            const msg = e?.response?.data?.message || "답변 수정 실패";
            this.replyErrorMessage = msg;
            alert(msg);
        } finally {
            this.isReplySaving = false;
        }
    },

    async deleteReply() {
        if (!this.selectedInquiry?.inquiryId) return;
        if (!confirm("답변을 삭제하시겠습니까?")) return;

        try {
            await axios.delete(`/api/admin/inquiries/${this.selectedInquiry.inquiryId}/reply`);

            // ✅ UI 즉시 반영 (로컬 상태 먼저 바꾸기)
            this.inquiryDetail.replyId = null;
            this.inquiryDetail.replyContent = null;
            this.replyText = "";
            this.isReplyReadonly = false;
            await this.setInquiryStatus(this.selectedInquiry.inquiryId, "OPEN"); // 답변 삭제 => 열람(미답변)

            // ✅ 목록도 다시 로딩
            await this.fetchInquiries();

            // ✅ 모달 닫기(선택)
            this.closeInquiryReply();

            alert("답변이 삭제되었습니다.");
        } catch (e) {
            console.error(e);
            alert(e?.response?.data?.message || "답변 삭제 실패");
        }
    },

    async refreshInquiryDetailAndList() {
        // detail 재조회
        const res = await axios.get(`/api/admin/users/inquiries/detail/${this.selectedInquiry.inquiryId}`);
        this.inquiryDetail = res.data;

        // 모달 상단(선택된 목록 row 데이터)도 상태 동기화 (답변 완료면 ANSWERED로)
        // 서버가 detail에 status를 내려준다는 가정. 없다면 아래 줄은 지워도 됨.
        if (this.inquiryDetail?.status) {
            this.selectedInquiry.status = this.inquiryDetail.status;
        } else {
            // 최소한 “답변 생김/없음”은 UI에 반영 가능
            this.selectedInquiry.status = this.inquiryDetail.replyId ? "ANSWERED" : this.selectedInquiry.status;
        }

        // 목록 전체 갱신(카운트도 같이 맞춤)
        await this.fetchInquiries();
    },

    isAnswered(inq) {
        if (!inq) return false;
        if (inq.status === "ANSWERED") return true;
        // 혹시 목록 DTO에 replyId/maxReplyId 같은게 있으면 추가로 커버 가능
        return false;
    },

    // ✅ inquiry paging 상태
    inquiryPage: {
        page: 1,
        size: 10,
        totalPages: 1,
        totalElements: 0
    },

    get inquiryPageNumbers() {
        const total = this.inquiryPage.totalPages || 1;
        const cur = this.inquiryPage.page || 1;

        // 현재 페이지 기준으로 5개만 보여주기
        let start = Math.max(1, cur - 2);
        let end = Math.min(total, start + 4);
        start = Math.max(1, end - 4);

        const arr = [];
        for (let i = start; i <= end; i++) arr.push(i);
        return arr;
    },

    goInquiryPage(p) {
        const n = Number(p);
        if (!n || n < 1) return;
        this.inquiryPage.page = n;
        this.fetchInquiries();
    },

    resetInquiryPage() {
        this.inquiryPage.page = 1;
    },


    // ✅ 답변 API 로딩/에러 (선택)
    isReplySaving: false,
    replyErrorMessage: "",

    // 임시: 답변 작성자 userId (로그인 연동 전까지)

    closeInquiryReply() {
        this.adminSubpage = "";
        this.selectedInquiry = null;
        this.inquiryDetail = null;
        this.replyText = "";
        this.isReplyReadonly = false;
    },

    // inquiry 모달 관련
    selectedInquiry: null,
    inquiryDetail: null,     // ✅ detail API 결과
    replyText: "",
    isReplyReadonly: false,  // ✅ 핵심

    // 유저 관리 검색/필터
    userSearchQuery: "",
    userFilterStatus: "all", // all | active | frozen

    async toggleFreeze(u) {
        // u.isFrozen = !u.active 라는 전제
        // frozen -> 해제(active=true), active -> 동결(active=false)
        const nextActive = u.isFrozen; // frozen(true)이면 nextActive=true(해제)

        // ✅ 혹시라도 프론트에서 안전장치(탈퇴 유저 버튼 비활성화)
        const email = String(u.email || "").toLowerCase();
        if (email.startsWith("deleted_") && email.endsWith("@deleted.local")) {
            alert("탈퇴한 사용자는 상태 변경할 수 없습니다.");
            return;
        }

        try {
            await axios.put(`/api/admin/users/${u.id}/active`, null, {
                params: { active: nextActive },
                withCredentials: true, // 같은 도메인이면 없어도 되지만 넣어도 OK
            });

            // 성공하면 UI 반영
            u.active = nextActive;
            u.isFrozen = !nextActive;
        } catch (e) {
            const msg = e?.response?.data?.message || "상태 변경 실패";
            alert(msg);
            console.error(e);
        }
    },
});
