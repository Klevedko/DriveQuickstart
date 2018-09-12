package map;

public class FileIdMap {
    private String id;
    private String name;
    private String webViewLink;
    private String idreal_owner;
    private String idowners;
    private boolean idInovus;

    public FileIdMap(String id, String name, String webViewLink, String idreal_owner, String idowners, Boolean idInovus) {
        this.id = id;
        this.name = name;
        this.webViewLink = webViewLink;
        this.idreal_owner = idreal_owner;
        this.idowners = idowners;
        this.idInovus = idInovus;
    }

    public FileIdMap(String id, String name, String webViewLink) {
        this.id = id;
        this.name = name;
        this.webViewLink = webViewLink;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getIdreal_owner() {
        return idreal_owner;
    }

    public void setIdreal_owner(String idreal_owner) {
        this.idreal_owner = idreal_owner;
    }

    public String getIdowners() {
        return idowners;
    }

    public void setIdowners(String idowners) {
        this.idowners = idowners;
    }

    public Boolean getIdInovus() {
        return idInovus;
    }

    public void setIdInovus(Boolean idInovus) {
        this.idInovus = idInovus;
    }
}
