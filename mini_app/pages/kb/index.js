const { kbCategories, kbArticles } = require('../../utils/mock');

Page({
  data: {
    categories: kbCategories,
    allArticles: kbArticles,
    articleList: kbArticles,
    activeCategory: 0,
    keyword: ''
  },

  onKeywordInput(event) {
    this.setData({ keyword: event.detail.value });
  },

  onCategoryTap(event) {
    const id = Number(event.currentTarget.dataset.id);
    this.setData({ activeCategory: id }, this.filterArticles);
  },

  onSearch() {
    this.filterArticles();
  },

  filterArticles() {
    const { activeCategory, keyword, allArticles } = this.data;
    const normalizedKeyword = keyword.trim().toLowerCase();

    const articleList = allArticles.filter((item) => {
      const hitCategory = activeCategory === 0 || item.categoryId === activeCategory;
      const hitKeyword =
        normalizedKeyword.length === 0 ||
        item.title.toLowerCase().includes(normalizedKeyword) ||
        item.summary.toLowerCase().includes(normalizedKeyword) ||
        item.keywords.join(',').toLowerCase().includes(normalizedKeyword);

      return hitCategory && hitKeyword;
    }).map((item) => ({
      ...item,
      keywordsText: item.keywords.join(' / ')
    }));

    this.setData({ articleList });
  },

  onLoad() {
    this.filterArticles();
  }
});
