window.communityPage = function () {
    return {
        view: 'list',
        user: { id: 'user_01', name: '山田 太郎' },

        posts: [
            {
                id: 1,
                title: '英語の勉強が捗った！',
                author: '山田 太郎',
                authorId: 'user_01',
                date: '2024.05.21',
                likes: 10,
                liked: false,
                content: '今日はリスニングを2時間。',
                comments: [
                    { id: 1, user: '運営', text: 'いいですね！' }
                ]
            }
        ],

        selectedPost: null,
        newComment: '',
        form: { title: '', content: '' },

        openPost(post) {
            this.selectedPost = post
            this.view = 'detail'
        },

        toggleLike(post) {
            post.liked = !post.liked
            post.likes += post.liked ? 1 : -1
        },

        addComment() {
            if (!this.newComment) return
            this.selectedPost.comments.push({
                id: Date.now(),
                user: this.user.name,
                text: this.newComment
            })
            this.newComment = ''
        },

        submitPost() {
            if (!this.form.title || !this.form.content) return

            if (this.view === 'edit') {
                this.selectedPost.title = this.form.title
                this.selectedPost.content = this.form.content
            } else {
                this.posts.unshift({
                    id: Date.now(),
                    title: this.form.title,
                    content: this.form.content,
                    author: this.user.name,
                    authorId: this.user.id,
                    date: new Date().toLocaleDateString(),
                    likes: 0,
                    liked: false,
                    comments: []
                })
            }

            this.form = { title: '', content: '' }
            this.view = 'list'
        },

        deletePost() {
            if (!confirm('削除しますか？')) return
            this.posts = this.posts.filter(p => p.id !== this.selectedPost.id)
            this.view = 'list'
        }
    }
}
