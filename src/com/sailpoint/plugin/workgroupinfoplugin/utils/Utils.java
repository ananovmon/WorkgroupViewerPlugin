package com.sailpoint.plugin.workgroupinfoplugin.utils;

import static sailpoint.api.SailPointFactory.getCurrentContext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import sailpoint.api.IdentityService;
import sailpoint.api.PasswordGenerator;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.api.Workflower;
import sailpoint.object.Alert;
import sailpoint.object.Application;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.Link;
import sailpoint.object.PasswordPolicy;
import sailpoint.object.PasswordPolicyHolder;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.QueryOptions;
import sailpoint.object.Workflow;
import sailpoint.object.WorkflowLaunch;
import sailpoint.plugin.PluginBaseHelper;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

/**
 * @author prashant.kagwad
 *
 *         Utility class.
 */
public class Utils {
	
	private static final Log	log		= LogFactory.getLog(Utils.class);
	
	private static Gson			gson	= new Gson();
	
	private Utils() {
		
	}

}
