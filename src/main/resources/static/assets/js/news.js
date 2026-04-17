// /assets/js/news.js
console.log('🔥 news.js loaded')

window.newsPage = function () {
    return {
        subpage: "",
        newsCategory: "",
        newsItems: [],
        selectedNews: null,
        isLoading: false,
        errorMsg: "",

        openCategory(cat) {
            this.newsCategory = cat;
            this.subpage = "list";
            this.loadNews();
        },

        backToCategory() {
            this.subpage = "";
            this.newsItems = [];
            this.selectedNews = null;
        },

        openDetail(item) {
            this.selectedNews = item;
            this.subpage = "detail";
        },

        async loadNews() {
            this.isLoading = true;
            this.errorMsg = "";
            try {
                const res = await fetch(`/api/notices?cat=${encodeURIComponent(this.newsCategory)}`);
                if (!res.ok) throw new Error(await res.text());
                this.newsItems = await res.json();
            } catch (e) {
                console.error(e);
                this.newsItems = [];
                this.errorMsg = "공지 불러오기 실패";
            } finally {
                this.isLoading = false;
            }
        },

    }
}
