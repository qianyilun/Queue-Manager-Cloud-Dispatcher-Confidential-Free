package com.allen.dsm;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.allen.template.PersistenceWithTemplate;
import com.sap.security.core.server.csi.IXSSEncoder;
import com.sap.security.core.server.csi.XSSEncoder;

/**
 * Servlet implementation class PersistencyWithDSM
 */
public class PersistenceWithDSM extends PersistenceWithTemplate {
	private static final long serialVersionUID = 1L;
	private static final String LINKNAME = "persistencewithdsm";
	private static final String COMPONENT = "DSM";
	private static final int FIXEDVALUE = 9999;
	private DSMDAO dsmDAO; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PersistenceWithDSM() {
        super();
        // TODO Auto-generated constructor stub
    }

    /** {@inheritDoc} */
    @Override
    public void init() throws ServletException {
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");
            dsmDAO = new DSMDAO(ds);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void displayTable(HttpServletResponse response) throws SQLException, IOException {
        // Append table that lists all persons
        List<DSM> resultList = dsmDAO.selectAllEntries();
        response.getWriter().println(
                "<p><center><table width=70% border=\"1\"><tr><th colspan=\"1\"></th>" + "<th colspan=\"3\">" + (resultList.isEmpty() ? "" : resultList.size() + " ")
                        + "Entries in the Database</th>"
                        + "<th colspan=\"3\">" + "Smart Sorted</th></tr>");
        if (resultList.isEmpty()) {
            response.getWriter().println("<tr><td colspan=\"4\">Database is empty</td></tr>");
        } else {
            response.getWriter().println("<tr><th>#</th><th>Name</th><th>Increase</th><th>Decrease</th><th>Amount</th><th>Total</th><th>Score</th></tr>");
        }
        IXSSEncoder xssEncoder = XSSEncoder.getInstance();
        int index = 1;
        Collections.sort(resultList); 
     
        // Add reset button
        response.getWriter().println("<p><center><form action=\"" + LINKNAME + "?operation=reset\" method=\"post\">" + "<input type=\"submit\" onclick=\"return window.confirm('Are you sure to RESET all values?')\" value=\"RESET\" />" + "</form></center></p>");

        for (DSM dsm : resultList) {        	
        	// Get score
        	String score = "0";
        	if (dsm.getDsm() != 0) {
        		double express = dsm.getDsm() * 0.80 + (dsm.getTotal()-dsm.getDsm())/dsm.getDsm() * 0.20 + 10;
        		DecimalFormat df = new DecimalFormat("#.###");
        		score = df.format(express); 	
        	}
        	
        	if (dsm.getDsm() < FIXEDVALUE) {
        		response.getWriter().println("<tr><td height=\"30\"><center>" + (index++) + "</center></td>");
	        	response.getWriter().println("<td height=\"30\"><center>" + xssEncoder.encodeHTML(dsm.getName()) + "</center></td>");
	        	response.getWriter().println("<td><center><form action=\"" + LINKNAME + "?Id="+ dsm.getId() + "&operation=add\" method=\"post\">" + "<input type=\"submit\" value=\"Add\" />" + "</form></center></td>"); 
	        	response.getWriter().println("<td><center><form action=\"" + LINKNAME + "?Id="+ dsm.getId() + "&operation=decrease\" method=\"post\">" + "<input type=\"submit\" value=\"Delete\" />" + "</form></center></td>"); 
	        	response.getWriter().println("<td height=\"30\"><center>" + dsm.getDsm() + "</center></td>");
	//			response.getWriter().println("<td height=\"30\"><center>" + dsm.getSum() + "</center></td>" + "<td height=\"30\"><center>" + score + "</center></td>");
	        	response.getWriter().println("<td height=\"30\"><center>" + score + "</center></td>");
	        	response.getWriter().println("<td><center><form action=\"" + LINKNAME + "?Id="+ dsm.getId() + "&operation=ignore\" method=\"post\">" + "<input type=\"submit\" onclick=\"return window.confirm('This person will be in vacation and you can undo anytime!')\" value=\"vacation\" />" + "</form></center></td>");
        	} else {
	        	response.getWriter().println("<tr><td height=\"30\"><center>" + (index++) + "</center></td>");
	        	response.getWriter().println("<td height=\"30\"><center>" + xssEncoder.encodeHTML(dsm.getName() + ": VACATION") + "</center></td>");
	        	response.getWriter().println("<td><center>"+ xssEncoder.encodeHTML("N/A") + "</center></td>"); 
	        	response.getWriter().println("<td><center>"+ xssEncoder.encodeHTML("N/A") + "</center></td>"); 
	        	response.getWriter().println("<td><center>"+ xssEncoder.encodeHTML("N/A") + "</center></td>");
				response.getWriter().println("<td><center>"+ xssEncoder.encodeHTML("N/A") + "</center></td>");
				response.getWriter().println("<td><center><form action=\"" + LINKNAME + "?Id="+ dsm.getId() + "&operation=undo\" method=\"post\">" + "<input type=\"submit\" value=\"undo\" />" + "</form></center></td>");
        	}
        	
			response.getWriter().println("</tr>");
        }
        response.getWriter().println("</table></center></p></body>");
        
             
        // Home button
        response.getWriter().println("<p><center><form action=\"" + "persistencewithnw" + "?operation=reset\" method=\"post\">" + "<input type=\"submit\" value=\"Return to Home\" />" + "</form></center></p>");
        
    }
    
    @Override
    protected void doIncrease(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        // Extract name of person to be added from request
        String id = request.getParameter("Id");
        if (id != null && !id.trim().isEmpty()) {
        	int ID = Integer.parseInt(id);
        	int amount = dsmDAO.getAmount(COMPONENT, ID) + 1;
        	dsmDAO.updateIncidentToPerson(id, amount, COMPONENT);
        }
    }

	@Override
	protected void doDecrease(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		// TODO Auto-generated method stub
		String id = request.getParameter("Id");
        if (id != null && !id.trim().isEmpty()) {
        	int ID = Integer.parseInt(id);
        	int amount = dsmDAO.getAmount(COMPONENT, ID) - 1;
        	dsmDAO.updateIncidentToPerson(id, amount, COMPONENT);
        }
	}

	@Override
	protected void doReset(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		// TODO Auto-generated method stub
		dsmDAO.resetIncidentToAll(COMPONENT);
	}

	@Override
	protected void doUndo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		// TODO Auto-generated method stub
		String id = request.getParameter("Id");
        if (id != null && !id.trim().isEmpty()) {
        	int ID = Integer.parseInt(id);
        	int amount = dsmDAO.getAmount(COMPONENT, ID) - FIXEDVALUE;
        	dsmDAO.updateIncidentToPerson(id, amount, COMPONENT);
        }
	}

	@Override
	protected void doIgnore(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		// TODO Auto-generated method stub
		String id = request.getParameter("Id");
        if (id != null && !id.trim().isEmpty()) {
        	int ID = Integer.parseInt(id);
        	int amount = dsmDAO.getAmount(COMPONENT, ID) + FIXEDVALUE;
        	dsmDAO.updateIncidentToPerson(id, amount, COMPONENT);
        }
	}

}