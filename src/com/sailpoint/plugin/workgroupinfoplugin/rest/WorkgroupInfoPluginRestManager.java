package com.sailpoint.plugin.workgroupinfoplugin.rest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.*;
import sailpoint.api.*;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.GeneralException;

import sailpoint.rest.plugin.AllowAll;

/**
 * @author 
 *
 *         Class to manage all rest api calls.
 */
@Path("WorkGroupInfoPlugin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllowAll
// @RequiredRight("workgroupinfopluginRight")
public class WorkgroupInfoPluginRestManager extends BasePluginResource {

	private static final Log log = LogFactory.getLog(WorkgroupInfoPluginRestManager.class);
	private Response response;

	public String getPluginName() {

		return "WorkGroup Info Plugin";
	}

	///// ENTRY method para ver si es un problema del post/Csrf, o del enrutamiento
	///// de sailpoint
	@GET
	@Path("ping")
	@AllowAll
	public Response ping() {
		Map<String, String> message = new HashMap<>();
		message.put("status", "ok");
		message.put("message", "Ping successful!");
		return Response.ok(message).build();
	}

	////// hasta aqui

	@GET
	@Path("workgroupMembers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkgroupMembers(@QueryParam("name") String name) {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "ok");

		try {
			SailPointContext context = getContext();
			if (name == null || name.trim().isEmpty()) {
				response.put("status", "error");
				response.put("message", "Nombre no proporcionado");
				return Response.ok(response).build();
			}

			Identity workgroup = context.getObjectByName(Identity.class, name);
			if (workgroup == null) {
				response.put("status", "error");
				response.put("message", "No se encontró la identidad/workgroup: " + name);
				return Response.ok(response).build();
			}

			java.util.List<String> memberNames = new java.util.ArrayList<>();
			if (workgroup.isWorkgroup()) {
				QueryOptions qo = new QueryOptions();
				qo.addFilter(Filter.eq("workgroups.name", workgroup.getName()));
				java.util.Iterator<Identity> it = context.search(Identity.class, qo);
				while (it != null && it.hasNext()) {
					Identity member = it.next();
					memberNames.add(member.getDisplayName() != null ? member.getDisplayName() : member.getName());
				}
				if (it != null) {
					sailpoint.tools.Util.flushIterator(it);
				}
				response.put("isWorkgroup", true);
			} else {
				// No es un workgroup, es un usuario individual (manager, etc.)
				memberNames.add(workgroup.getDisplayName() != null ? workgroup.getDisplayName() : workgroup.getName());
				response.put("isWorkgroup", false);
			}

			response.put("members", memberNames);
			response.put("objectName",
					workgroup.getDisplayName() != null ? workgroup.getDisplayName() : workgroup.getName());

		} catch (GeneralException e) {
			response.put("status", "error");
			response.put("message", "Error de SailPoint: " + e.getMessage());
		}

		return Response.ok(response).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("data")
	public Response postExample(Map<String, Object> colorPalette)
			throws GeneralException, SQLException, ParseException {
		try {
			String c1 = (String) colorPalette.get("c1");
			String c2 = (String) colorPalette.get("c2");
			String c3 = (String) colorPalette.get("c3");
			String c4 = (String) colorPalette.get("c4");

			String catalinaBase = System.getProperty("catalina.base");
			String cssFile = catalinaBase != null
					? catalinaBase + "/webapps/identityiq/ui/css/ui-custom.css"
					: "C:/tomcat/webapps/identityiq/ui/css/ui-custom.css"; // Fallback

			String cssString = "/*Controls the styling for Angular pages in the desktop and mobile UI */\n" +
					"\n" +
					"/*\n" +
					"primary = " + c1 + " -> main/base (green where navy blue is used in default scheme)\n" +
					"primary 2 shades darker = " + c2 + " -> used for highlights/hover\n" +
					"secondary = " + c3 + " (orange where light blue is used in default scheme)\n" +
					"secondary 2 shades darker = +" + c4 + " -> highlight/hover\n" +
					"\n" +
					"gray = #808080 -> accents\n" +
					"white = #fff -> accents\n" +
					"*/\n" +
					"\n" +
					"/**********************************************************\n" +
					"   Common UI Components\n" +
					"**********************************************************/\n" +
					"/* Primary Buttons (white text on colored background) */\t\n" +
					".btn-primary { background-color: " + c1 + "; border-color: " + c1 + ";}\n" +
					".btn-primary:hover,\n" +
					".btn-primary:focus,\n" +
					".btn-primary:active {\n" +
					"  background-color: " + c2 + "; border-color: " + c1 + ";}\n" +
					"\n" +
					"/* Loading Data message - this is seen in panels on refresh (as an example) */\n" +
					".alert-info {\n" +
					"  color: " + c3 + ";\n" +
					"}\n" +
					"\n" +
					"/* Focused or hovered links */\n" +
					"a:hover, a:focus {\n" +
					"  color: +" + c4 + ";\n" +
					"}\n" +
					"\n" +
					" /* Disabled primary button */\n" +
					".btn-info.disabled, .btn-info[disabled] {background-color: " + c3 + "}\n" +
					"\n" +
					"\n" +
					"/**********************************************************\n" +
					"   Login Page (mobile and desktop)\n" +
					"**********************************************************/\t\n" +
					"\n" +
					"/* Header bar around and line under logo on login page; not usually modified */\n" +
					".header\n" +
					"{ \n" +
					"  min-height: 50px;\n" +
					"  /*  background-color: " + c1 + "; \n" +
					"  border-bottom-color: " + c1 + " */ \n" +
					"}  \n" +
					".header .nav-brand img {max-height: 40px}\n" +
					"\n" +
					"/* Center box on login page */\n" +
					".login-welcome {color: " + c3 + " } \n" +
					"\n" +
					".form-control:focus {border-color: " + c3 + ";}\n" +
					"\n" +
					"#loginForm\\:loginButton {background-color: " + c3 + "; border-color: " + c3 + "}\n" +
					"#loginForm\\:loginButton.btn-info:hover, \n" +
					"#loginForm\\:loginButton.btn-info:focus {\n" +
					"  background-color: +" + c4 + " ;\n" +
					"}\n" +
					"\n" +
					"/* All optional links on Login page (Forgot password, Unlock Account, New User Registration) */\n"
					+
					"#loginForm a.text-info {color:" + c3 + " }\n" +
					"#loginForm a.text-info:hover, \n" +
					"#loginForm a.text-info:focus {color:+" + c4 + " }\n" +
					"\n" +
					".fa-blue {color: " + c3 + "}\n" +
					"\n" +
					"/* Use if login page center logo is a square image */\n" +
					".img-circle {border-radius: 0%;}\n" +
					"\n" +
					"/**********************************************************\n" +
					" Desktop Only Angular Pages \n" +
					"**********************************************************/\n" +
					"\n" +
					"/* size the topbar logo images */\n" +
					".topbar img {\n" +
					"  max-width: 100px !important;\n" +
					"}\n" +
					"\n" +
					"/* Main Menu header bar in Desktop body pages */\n" +
					".bg-primary { background-color: " + c1 + "  } \n" +
					"\n" +
					"/* Desktop Main Menu Bar options and sub-options when selected/open */\n" +
					".bg-primary .nav .open > a {background-color: " + c2 + "}\n" +
					".dropdown-menu .menuitem:hover {background-color:" + c1 + " }\n" +
					".nav > .dropdown > .dropdown-toggle:focus:not(:hover) {background-color: " + c2
					+ "; color:#ffffff}\n" +
					".bg-primary .nav li > ul.dropdown-menu > li > a {color: " + c1 + "}\n" +
					".bg-primary .nav li > a:focus, .bg-primary .nav li > a:hover {background-color: " + c2
					+ " ; color:#ffffff }\n" +
					"\n" +
					"/* Quicklink Panel List Button in Desktop main menu bar*/\n" +
					"#quicklinkButton:hover:not(.collapsed), \n" +
					"#quicklinkButton:focus:not(.collapsed),\n" +
					"#quicklinkButton:not(.collapsed) {background-color: " + c2 + "}\n" +
					"\n" +
					"/* Quicklink Panel */\n" +
					".bg-primary.lt,\n" +
					"#quicklinkPanel .panel-group .panel .panel-heading,\n" +
					"#quicklinkPanel .panel-group .panel .panel-body {background-color: " + c1 + "}\n" +
					"#quicklinkPanel .panel-group .panel .panel-body a:focus, \n" +
					"#quicklinkPanel .panel-group .panel .panel-body a:hover {background-color: " + c2 + "}\n" +
					"\n" +
					"/* Desktop Home Page */\n" +
					".card-title, .card-arrow-icon {color: " + c3 + "}\n" +
					"\n" +
					".quicklink-card-link .panel:hover ,\n" +
					".quicklink-card-link .panel:focus,\n" +
					".quicklink-card-link:hover .panel,\n" +
					".home-edit-card-link:hover .panel,\n" +
					".quicklink-card-link:focus .panel,\n" +
					".home-edit-card-link:focus .panel,\n" +
					".quicklink-card-link a:hover .panel,\n" +
					".home-edit-card-link a:hover .panel,\n" +
					".quicklink-card-link a:focus .panel,\n" +
					".home-edit-card-link a:focus .panel \n" +
					"{\n" +
					"  border-color: " + c3 + ";\n" +
					"  background: " + c3 + ";\n" +
					"  color: #ffffff ;\n" +
					"}\n" +
					"\n" +
					"/* Drop-down menu on violation work items Widget header */\n" +
					".text-info {color: " + c3 + " }\n" +
					".btn-link:hover,\n" +
					".btn-link:focus {\n" +
					"  color: " + c3 + " ;\n" +
					"  text-decoration: underline;\n" +
					"  background-color: transparent ;\n" +
					"}\n" +
					".dropdown > .dropdown-toggle:focus:not(:hover) { background-color: #f9fafc}\n" +
					".dropdown-menu > li > a:hover, .dropdown-menu > li > a:focus { background-color: " + c3 + "}\n" +
					"\n" +
					"/*Widget list hovers*/\n" +
					"a.list-group-item.ng-scope:hover, a.list-group-item.ng-scope:focus \n" +
					" {background-color:" + c3 + " !important }\n" +
					"\n" +
					"/* Edit Home page */\n" +
					".home-edit-card .panel-heading {color:" + c3 + "}\n" +
					".home-edit-card-link a .card-title {color:" + c3 + "}\n" +
					"\n" +
					"\n" +
					"/* NOTE: btn-info could affect a wide array of buttons, but is the only tag available for \n" +
					"   controlling the Save button on the Edit Home page */\n" +
					".btn-info, .btn-info.active,.btn-info:active\n" +
					"     { background-color: " + c3 + "; border: " + c3 + ";}\n" +
					"\n" +
					".btn-info:hover,.btn-info:focus {\n" +
					"  background-color: +" + c4 + ";\n" +
					"}\n" +
					"\n" +
					"/*Add Card checkboxes */\n" +
					"sp-checkbox button.checkbox-btn:hover,\n" +
					"sp-checkbox button.checkbox-btn + label:hover,\n" +
					"sp-checkbox button.checkbox-btn:active,\n" +
					"sp-checkbox button.checkbox-btn + label:active\n" +
					" {color:#333333 !important}\n" +
					"\n" +
					"/******************************************\n" +
					"  Access Request Pages (mobile and desktop)\n" +
					"******************************************/\n" +
					"/* Access Request Progress Bar (Select Users, Manage Access, Review) */\n" +
					"\n" +
					".btn-group-progress > .btn-table-cell > button {color:" + c3 + "}\n" +
					".btn-group-progress > .btn-table-cell > button.progress-active, .btn-group-progress > .btn-table-cell > button:hover {!important;background-color: "
					+ c3 + "; color: #ffffff; }\n" +
					".btn-group-progress > .btn-table-cell > button.progress-active:before, .btn-group-progress > .btn-table-cell > button:hover:before { !important;border-left-color: "
					+ c3 + "; }\n" +
					".btn-group-progress > .btn-table-cell > button.enabled {color: " + c3 + "}\n" +
					".access .btn-link {color:" + c3 + "}\n" +
					"\n" +
					"/* Badge on Review tab of Manage User Access page (mobile and desktop) */\n" +
					".badge.bg-info {background-color: " + c3 + " ;}\n" +
					"\n" +
					".btn-progress:hover .badge.bg-info, .btn-progress:focus .badge.bg-info, btn-progress:active .badge.bg-info, \n"
					+
					".btn-progress.progress-active:not(:focus) .badge.bg-info   {background-color: #ffffff ; color: "
					+ c3 + " }\n" +
					"\n" +
					"/* Sub-menu on Manage Access page (Add access/Remove access */\n" +
					".btn-group.sub-nav .btn.active, \n" +
					".btn-group.sub-nav .btn:focus { color: " + c3 + " !important }\n" +
					"\n" +
					"/* page navigation within access request pages */\n" +
					".pagination > li > a { color:" + c3 + " }\n" +
					".pagination > li > a:hover { color:+" + c4 + " }\n" +
					".pagination > .active > a,\n" +
					".pagination > .active > a:focus {background-color: " + c3 + " ;  border-color: " + c3 + " }\n" +
					".pagination > .active > a:hover, \n" +
					".pagination > .active > a:focus:hover {background-color: +" + c4 + " ; border-color: " + c3
					+ "  }\n" +
					"\n" +
					"\n" +
					"/***********************************************\n" +
					" Edit Identity, Accounts, Change Password page \n" +
					"************************************************/\n" +
					"/*Button on Identity Cards */\n" +
					".identity-card .panel-footer .btn-info {background-color: " + c3 + "}\n" +
					".identity-card .panel-footer .btn-info:hover {background-color: +" + c4 + "}\n" +
					"\n" +
					"/* Edit Identity, Accounts, Change Password page left menu */\n" +
					".identity .sidebar .list-group li a:hover {color: +" + c4 + "}\n" +
					".identity .sidebar .list-group-item.active:hover a, .list-group-item.active:focus a {color: #ffffff}\n"
					+
					".identity .sidebar .list-group-item.active {background-color: " + c3 + " !important; border-color:"
					+ c3 + " !important}\n" +
					"\n" +
					"/* Password application count badge */\n" +
					".identity .identity-details-container .panel-heading .label-info-solid {background-color: " + c3
					+ " }\n" +
					"\n" +
					"/* Forwarding User pick list */\n" +
					".dropdown-menu > li > a:hover,\n" +
					".dropdown-menu > li > a:focus,\n" +
					".dropdown-menu > li.active > a,\n" +
					".dropdown-menu > li.active > a:hover,\n" +
					".dropdown-menu > li.active > a:focus {background-color: " + c3 + "}\n" +
					"\n" +
					"/* Role hierarchy (modal box content in role details) */\n" +
					"a.angular-ui-tree-node-content, \n" +
					"a.angular-ui-tree-node-content.angular-ui-tree-node-selected  {color: " + c1 + "}\n" +
					"a.angular-ui-tree-node-content:hover {color: " + c2 + "}\n" +
					"\n" +
					"/* Manage Passwords checkbox */\n" +
					"button.checkbox-btn:focus, button.checkbox-btn:hover {color: " + c3 + "}\n" +
					"\n" +
					"\n" +
					"/****************************\n" +
					"Violation Work Items Page\n" +
					"****************************/\n" +
					".decision-page .decision-page-tabs > li.active > button {color:" + c3 + "}\n" +
					".decision-page .decision-page-tabs > li.active .label {background-color:" + c3 + "}\n" +
					".decision-page .decision-page-tabs > li > button:hover {color:" + c3 + "}\n" +
					"\n" +
					"/*****************\n" +
					" Gear Menu\n" +
					" *****************/\n" +
					"/* Global Settings menu page hover over/click options */\n" +
					"a.list-group-item:focus, a.list-group-item:hover {\n" +
					"  color: +" + c4 + ";\n" +
					"}\n" +
					"\n" +
					"/* Admin Console Headers */\n" +
					".header .nav-tabs li > a:hover {color:+" + c4 + " }\n" +
					".header .nav-tabs li.active > a:hover {color:#717171}\n" +
					".header > .nav-tabs > li > a {color:" + c3 + "}\n" +
					"\n" +
					".panel-heading .nav-tabs > li > a {color: #717171} \n" +
					".panel-heading .nav-tabs > li.active > a,\n" +
					".panel-heading .nav-tabs > li.active  {color: " + c3 + "}\n" +
					".panel-heading .nav-tabs > li > a:hover,\n" +
					".panel-heading .nav-tabs > li.active > a:hover, \n" +
					".panel-heading .nav-tabs > li.active > a:focus {color: +" + c4 + "}\n" +
					"\n" +
					"\n" +
					"/* Quicklink Populations */\n" +
					"/* List of QL Populations */\n" +
					"#qlpEditorDiv .selectableGrid .x-grid-row-selected.x-grid-row-focused .x-grid-cell { background-color: "
					+ c3 + " !important;}\n" +
					"\n" +
					"/* Section headers on Configuration tab */\n" +
					"#qlpEditorDiv .spTable caption .m-l-xs {color: " + c3 + " }\n" +
					"\n" +
					"\n" +
					"/********************************************\n" +
					"  Mobile Pages\n" +
					"*********************************************/\n" +
					"\n" +
					"/* buttons in header bar (Home, Exit) */\n" +
					"a.btn-mobile {color: " + c1 + "}\n" +
					"a.btn-mobile:hover, a.btn-mobile:focus {color: " + c2 + "}\n" +
					"\n" +
					"/* Mobile Home Page menu options */\n" +
					".mobile-quick-links .card-title {color: " + c3 + "}\n" +
					".mobile-quick-links .quick-links-btn {border-color: " + c3 + "; color: " + c3 + "}\n" +
					".mobile-quick-links .card-title:hover, \n" +
					".mobile-quick-links .card-title:focus  {color: +" + c4 + "}\n" +
					".mobile-quick-links .quick-links-btn:hover,\n" +
					".mobile-quick-links .quick-links-btn:focus {border-color: " + c3 + "; background-color: +" + c4
					+ "; color: #ffffff}\n" +
					"\n" +
					"/* Dropdowns in Desktop main menu viewed in narrow browser window\n" +
					"   require this to make text visible by setting background white \n" +
					"   media query limits effect only to narrow browsers \n" +
					"   (lt or eq to 767 pixels = tablets and phones)*/\n" +
					"@media (max-width: 767px) {\n" +
					"   .bg-primary .nav .open .dropdown-menu  {background-color: #ffffff}\n" +
					"}\n" +
					"\n" +
					"/**************************************************\n" +
					"  Modal Dialog Boxes (use primary color for these\n" +
					"  to match main menu bar)\n" +
					" **************************************************/\n" +
					"/* Header bar */\n" +
					"/* NOTE: don't make too dark or the .close (little \"x\") will not show up well */\n" +
					"\n" +
					".modal-header,\n" +
					"div.info-modal[role=\"dialog\"] .modal-header,\n" +
					"div.warning-modal[role=\"dialog\"] .modal-header {\n" +
					"  background-color: " + c1 + ";\n" +
					"  border-color: " + c1 + ";\n" +
					"}\n" +
					"\n" +
					"/* Primary button in modal boxes */\n" +
					".modal-footer .btn-info , .modal-footer .btn-warning, .modal-body .panel-footer .btn-info {\n" +
					" background-color: " + c1 + ";\n" +
					" border-color: " + c1 + ";\n" +
					" }\n" +
					".modal-footer .btn-info:hover , .modal-footer .btn-warning:hover,\n" +
					".modal-footer .btn-info:focus , .modal-footer .btn-warning:focus,\n" +
					".modal-footer .btn-info:active , .modal-footer .btn-warning:active\n" +
					" {\n" +
					" background-color: " + c2 + ";\n" +
					" }\n" +
					"\n" +
					"/* Data entry box on modal forms */\n" +
					".modal-body .form-control:focus { border-color: " + c1 + "}\n" +
					"\n" +
					"/*Secondary option (usually Cancel) on some modal boxes \n" +
					"  Sometimes is colored link, sometimes just gray button; this\n" +
					"  is for colored link; gray button left alone since neutral */\n" +
					".modal-body .panel-footer .btn-link:hover, .modal-body .panel-footer .btn-link:focus,\n" +
					".modal-footer .btn-link:hover, .modal-footer .btn-link:focus {color: " + c1 + "}\n" +
					"\n" +
					"/*Sub headers in modal boxes*/\n" +
					".modal-content .panel-heading .nav-tabs > li > a,\n" +
					".modal-content .panel-heading .nav-tabs > li.active > a,\n" +
					".modal-content .panel-heading .nav-tabs > li.active  {color: " + c1 + "}\n" +
					".modal-content .panel-heading .nav-tabs > li > a:hover,\n" +
					".modal-content .panel-heading .nav-tabs > li.active > a:hover, \n" +
					".modal-content .panel-heading .nav-tabs > li.active > a:focus {color: " + c2 + "}\n" +
					"\n" +
					"/* drop-down list boxes on modal forms */\n" +
					".modal-content .dropdown-menu > li > a:hover,\n" +
					".modal-content .dropdown-menu > li > a:focus,\n" +
					".modal-content .dropdown-menu > li.active > a,\n" +
					".modal-content .dropdown-menu > li.active > a:hover,\n" +
					".modal-content .dropdown-menu > li.active > a:focus {background-color: " + c1 + "}\n" +
					"\n" +
					"/* Global setting: text-info links when hovered */\n" +
					"a.text-info:hover {color: +" + c4 + "}\n" +
					"\n" +
					"/* Alert Definition-> New; footer bar */\n" +
					"div.footer-action-bar {background-color: #d8dde5}\n" +
					"\n" +
					"\n" +
					"/*Access Review Identity List*/\n" +
					".certification .entity-list-panel .cert-id-list .active {background-color: " + c3 + "}\n" +
					".certification .entity-list-panel .cert-id-list > tbody > tr:focus,\n" +
					".certification .entity-list-panel .cert-id-list > tbody > tr:hover {background-color: " + c3 + "}";

			Map<String, String> message = new HashMap<>();

			System.out.println(cssString);

			System.out.println(colorPalette);

			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(cssFile));

				message.put("status", "ok");
				message.put("message", "Color changed");
				writer.write(cssString);
				writer.close();

				System.out.println("File written in: " + cssFile);
			} catch (IOException e) {
				message.put("status", "error");
				message.put("message", String.format("Error: %s", e.getMessage()));
				System.err.println("Error writing to the file -> " + e.getMessage());
			}

			return Response.status(Response.Status.OK).entity(message).build();
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}
}
