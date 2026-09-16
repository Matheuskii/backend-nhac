package br.com.nhac.backend_nhac.infra.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "firebase.storage")
public class StorageProperties {

    /** Nome do bucket do Firebase Storage, ex: "nhac-delivery.appspot.com". */
    private String bucketName;

    /** JSON da service account do Firebase, codificado em Base64 numa linha só. */
    private String credentialsBase64;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getCredentialsBase64() {
        return credentialsBase64;
    }

    public void setCredentialsBase64(String credentialsBase64) {
        this.credentialsBase64 = credentialsBase64;
    }
}
