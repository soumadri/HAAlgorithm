package com.filecluster.exception;

public final class ExceptionMessages {
	public final static String INVALID_NAME = "Please check the file/host name.\r\n1. Hostname and filename cannot be same\r\n2. Filename extensions must be from allowed list";
	public final static String TOO_MANY_COPIES = "Already 2 copies of this file is present in the cluster. "
												+ "Maximum 2 copies can be maintained";
	public final static String NO_HOST_FOUND = "No host with the given name exist in the cluster";
	public final static String TOO_MANY_FAILED_HOST = "Maximum number of failed hosts at any given point in time is 2";
	public final static String TOO_FEW_HOSTS = "Atleast 3 hosts need to be present in the cluster to perform HA";
}
