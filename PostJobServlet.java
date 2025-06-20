package controller;

import dao.JobDAO;
import model.Job;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class PostJobServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/view/AdminLogin.jsp");
            return;
        }
        
        // Get form data
        String title = request.getParameter("title");
        String company = request.getParameter("company");
        String location = request.getParameter("location");
        String type = request.getParameter("type");
        String category = request.getParameter("category");
        String description = request.getParameter("description");
        String requirements = request.getParameter("requirements");
        String salary = request.getParameter("salary");
        String deadline = request.getParameter("deadline");
        
        // Debug output to console
        System.out.println("Title: " + title);
        System.out.println("Company: " + company);
        System.out.println("Description: " + description);
        
        // Validate required fields
        if (title == null || title.trim().isEmpty() || 
            company == null || company.trim().isEmpty() ||
            description == null || description.trim().isEmpty()) {
            
            response.sendRedirect(request.getContextPath() + "/view/postjob.jsp?error=Title, company, and description are required");
            return;
        }
        
        try {
            // Set default status to Pending
            String jobStatus = "Pending";
            
            // Set posted date to current time
            Timestamp postedDate = new Timestamp(System.currentTimeMillis());
            
            // Set admin ID from session
            Integer adminId = (Integer) session.getAttribute("adminId");
            
            // Create Job object with constructor
            // Use 0 for jobId since it will be auto-generated by the database
            Job job = new Job(
                0,                  // jobId (will be set by database)
                title,              // title
                type,               // jobType
                description,        // description
                company,            // companyName
                location,           // location
                salary,             // salary
                requirements,       // requirements
                deadline,           // applicationDeadline
                jobStatus,          // jobStatus
                postedDate.toString(), // postedDate as string
                adminId             // postedBy
            );
            
            // Set deadline if provided
            if (deadline != null && !deadline.trim().isEmpty()) {
                try {
                    // Convert deadline string to Timestamp
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    Date parsedDate = dateFormat.parse(deadline);
                    Timestamp deadlineTimestamp = new Timestamp(parsedDate.getTime());
                    job.setApplicationDeadline(deadlineTimestamp.toString());
                } catch (Exception e) {
                    System.err.println("Error parsing deadline: " + e.getMessage());
                    // Continue without deadline if parsing fails
                }
            }
            
            // Use JobDAO to insert the job
            JobDAO jobDAO = new JobDAO();
            boolean success = jobDAO.insertJob(job);
            
            // If the job was successfully inserted, redirect to admin dashboard
            if (success) {
                System.out.println("Job posted successfully!");
                response.sendRedirect(request.getContextPath() + "/view/Admin_dashboard.jsp?message=Job%20Posted%20Successfully");
            } else {
                System.err.println("Failed to post job. No rows affected.");
                response.sendRedirect(request.getContextPath() + "/view/postjob.jsp?error=Failed%20to%20post%20the%20job");
            }
        } catch (Exception e) {
            System.err.println("Error posting job: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/view/postjob.jsp?error=Error: " + e.getMessage());
        }
    }
}
