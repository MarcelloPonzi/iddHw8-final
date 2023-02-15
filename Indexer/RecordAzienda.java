class RecordAzienda {
    private String name;
    private String marketCap;
    private String country;
    private String ceo;
    private String industry;
    private String webPage;
    private String foundedYear;
    private String stock;
    private String linkDB = "";

    public RecordAzienda(String name, String marketCap, String country, String ceo, String industry, String webPage, String foundedYear, String stock) {
        this.name = name;
        this.marketCap = marketCap;
        this.country = country;
        this.ceo = ceo;
        this.industry = industry;
        this.webPage = webPage;
        this.foundedYear = foundedYear;
        this.stock = stock;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMarketCap() {
        return this.marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCeo() {
        return this.ceo;
    }

    public void setCeo(String ceo) {
        this.ceo = ceo;
    }

    public String getIndustry() {
        return this.industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getWebPage() {
        return this.webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public String getFoundedYear() {
        return this.foundedYear;
    }

    public void setFoundedYear(String foundedYear) {
        this.foundedYear = foundedYear;
    }

    public String getStock() {
        return this.stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getLinkDB() {
        return this.linkDB;
    }

    public void setLinkDB(String linkDB) {
        this.linkDB = linkDB;
    }

    @Override
    public String toString() {
        return "RecordAzienda{" +
                "name='" + this.name + '\'' +
                ", marketCap='" + this.marketCap + '\'' +
                ", country='" + this.country + '\'' +
                ", ceo='" + this.ceo + '\'' +
                ", industry='" + this.industry + '\'' +
                ", webPage='" + this.webPage + '\'' +
                ", foundedYear='" + this.foundedYear + '\'' +
                ", stock='" + this.stock + '\'' +
                ", linkDB='" + this.linkDB + '\'' +
                '}';
    }
}