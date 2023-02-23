package com.mfw.atlas.client.utils;

import com.mfw.atlas.client.constants.EnvExceptionEnum;
import com.mfw.atlas.client.exceptions.EnvException;

public class EnvUtils {

    private static String instanceId;
    private static String ip;
    private static String envType;
    private static String envGroup;
    private static String appVersionId;
    private static String appCode;
    private static String containerCluster;
    private static String idc;
    private static String deployProvider;

    static {
        instanceId = System.getenv("HOSTNAME");
        ip = System.getenv("POD_IP");
        envType = System.getenv("APP_ENV_TYPE");
        envGroup = System.getenv("APP_ENV_GROUP");
        appVersionId = System.getenv("APP_VERSION_ID");
        containerCluster = System.getenv("K8S_CLUSTER_NAME");
        idc = System.getenv("APP_IDC");
        appCode = System.getenv("APP_CODE");
        deployProvider = System.getenv("APP_PROVIDER");
    }

    private void EnvUtils(){}

    public static String getInstanceId(){
        return instanceId;
    }

    public static String getIp() {
        return ip;
    }

    public static String getEnvType() {
        return envType;
    }

    public static String getEnvGroup() {
        return envGroup;
    }

    public static String getAppVersionId() {
        return appVersionId;
    }

    public static String getAppCode() {
        return appCode;
    }

    public static String getContainerCluster() {
        return containerCluster;
    }

    public static String getIdc() {
        return idc;
    }

    public static String getDeployProvider() {
        return deployProvider;
    }

    public static String getEnvInfomation() {
        return "instanceId:" + instanceId + " ip:" + ip + " envType:" + envType + " envGroup:" + envGroup
                + " appVersionId:" + appVersionId + " appCode:" + appCode + " containerCluster:" + containerCluster
                + " idc:" + idc + " deployProvider:" + deployProvider;
    }

    public static void validateEnv() throws EnvException{
        if (StringUtils.isNotBlank(appCode) && StringUtils.isNotBlank(envType) && StringUtils.isNotBlank(instanceId)
                && StringUtils.isNotBlank(appVersionId)){
        } else {
            throw new EnvException(EnvExceptionEnum.ENV_INCOMPLETE, "Env validate fail! "
                    + String.format("appCode:%s, envType:%s, envGroup:%s, appVersionId:%s",
                    appCode, envType, envGroup, appVersionId));
        }
    }

    public static boolean isEnvValiate() {
        return StringUtils.isNotBlank(appCode) && StringUtils.isNotBlank(envType) && StringUtils.isNotBlank(instanceId)
                && StringUtils.isNotBlank(appVersionId);
    }

    public static void validateApplicationName(String applicationName) throws EnvException{
        if (!appCode.equals(applicationName)){
            throw new EnvException(EnvExceptionEnum.ENV_INVALID_APPNAME, "ApplicationName validate fail! "
                    + String.format("appCode:%s, applicationName:%s", appCode, applicationName));
        }
    }

}
