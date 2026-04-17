window.loginPage = function () {
    return {
        email: '',
        password: '',

        login() {
            if (!this.email || !this.password) {
                alert('メールとパスワードを入力してください')
                return
            }

            alert('ログイン成功（仮）')
            location.href = '/mypage'
        }
    }
}
// assets/js/login.js
window.loginPage = function () {
    return {
        email: '',
        password: '',

        login() {
            if (!this.email || !this.password) {
                alert('メールアドレスとパスワードを入力してください')
                return
            }

            // 로그인 성공 처리 (Mock)
            localStorage.setItem('isLoggedIn', 'true')
            location.href = '/mypage'
        }
    }
}
