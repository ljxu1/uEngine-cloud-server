package org.uengine.cloud.tenant;

/**
 * Created by uengine on 2017. 11. 14..
 */
public class TenantContext {
    static ThreadLocal<TenantContext> local = new ThreadLocal();
    String tenantId;
    String userId;

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TenantContext(String tenantId) {
        this.tenantId = tenantId;
        local.set(this);
    }

    public static TenantContext getThreadLocalInstance() {
        TenantContext tc = (TenantContext) local.get();
        return tc != null ? tc : new TenantContext((String) null);
    }
}
