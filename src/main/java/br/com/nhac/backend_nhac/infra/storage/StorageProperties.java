package br.com.nhac.backend_nhac.infra.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "firebase.storage")
public class StorageProperties {

    private String bucketName;
    private Resource credentialsPath;      // dev: classpath:...json
    private String credentialsBase64;      // prod: Render env var

    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }

    public Resource getCredentialsPath() { return credentialsPath; }
    public void setCredentialsPath(Resource credentialsPath) { this.credentialsPath = credentialsPath; }

    public String getCredentialsBase64() { return credentialsBase64; }
    public void setCredentialsBase64(String credentialsBase64) { this.credentialsBase64 = credentialsBase64; }
}