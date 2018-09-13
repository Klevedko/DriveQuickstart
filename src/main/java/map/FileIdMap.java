package map;

import java.io.Serializable;

public class FileIdMap implements Serializable, Comparable<FileIdMap> {
    private String id;
    private String folderName;
    private String name;
    private String webViewLink;
    private String idreal_owner;
    private String idowners;
    private String goodOwnersList;
    private String badOwnersList;
    private boolean idInovus;

    public FileIdMap(String id, String name, String webViewLink, String idreal_owner, String idowners, String goodOwnersList, String badOwnersList, Boolean idInovus) {
        this.id = id;
        this.name = name;
        this.webViewLink = webViewLink;
        this.idreal_owner = idreal_owner;
        this.idowners = idowners;
        this.goodOwnersList = goodOwnersList;
        this.badOwnersList = badOwnersList;
        this.idInovus = idInovus;
    }

    public FileIdMap(String id, String folderName, String name, String webViewLink) {
        this.id = id;
        this.folderName = folderName;
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

    public String getGoodOwnersList() {
        return goodOwnersList;
    }

    public void setGoodOwnersList(String goodOwnersList) {
        this.goodOwnersList = goodOwnersList;
    }

    public String getBadOwnersList() {
        return badOwnersList;
    }

    public void setBadOwnersList(String badOwnersList) {
        this.badOwnersList = badOwnersList;
    }

    public boolean isIdInovus() {
        return idInovus;
    }

    public void setIdInovus(boolean idInovus) {
        this.idInovus = idInovus;
    }

    public Boolean getIdInovus() {
        return idInovus;
    }

    public void setIdInovus(Boolean idInovus) {
        this.idInovus = idInovus;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public boolean equals(Object obj) {
        FileIdMap that = (FileIdMap) obj;
        if (!(this.folderName.equals(that.folderName))
                ) return false;
        return true;
    }
    @Override
    //this is required to print the user friendly information about the map.AuditMap
    public String toString() {
        return "[date=" + this.folderName + "]";
    }

    @Override
    public int compareTo(FileIdMap o) {
        int result = this.folderName.compareToIgnoreCase(o.folderName);
        if (result != 0) {
            return result;
        } else {
            return new String(this.folderName).compareTo(new String(o.folderName));
        }
    }
}
