document.addEventListener('alpine:init', () => {
    Alpine.store('user', {
        id: 'user_01',
        name: '山田 太郎',
        email: 'yamada@example.com',
        isLoggedIn: true,
        subscriptionEnd: '2024-12-31'
    })
})

// assets/js/app.js
window.app = function () {
    return {
        isLoggedIn: localStorage.getItem('isLoggedIn') === 'true',

        // ✅ 페이지 상태 추가
        page: 'booking',      // 'booking' | 'subscription' 등
        bookingStep: 1,       // 진행바/예약 스텝을 nav에서도 제어하고 싶다면

        goBooking() {
            this.page = 'booking'
            this.bookingStep = 1
        },

        goSubscription() {
            this.page = 'subscription'
        },

        logout() {
            localStorage.removeItem('isLoggedIn')
            this.isLoggedIn = false
            location.href = '/'
        }
    }
}
