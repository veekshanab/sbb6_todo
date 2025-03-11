package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import beans.Register;
import beans.Task;
import factory.DBConn;

public class ToDoDAOImpl implements ToDoDAO {

	Connection con;
	Statement stmt;
	PreparedStatement pstmt1, pstmt2, pstmt3, pstmt4;
	ResultSet rs;
	static ToDoDAO toDoDAO;
	
	// to make class Singleton we declared constructor as private
	private ToDoDAOImpl() {
		try {
			con=DBConn.getConn();
			stmt=con.createStatement();
			pstmt1=con.prepareStatement("INSERT INTO register VALUES (?,?,?,?,?,?,?)");
			pstmt2=con.prepareStatement("INSERT INTO tasks VALUES (?,?,?,?,?)");
			pstmt3=con.prepareStatement("INSERT INTO taskid_pks VALUES (?,?) ");
			pstmt4=con.prepareStatement("UPDATE taskid_pks SET taskid=? WHERE regid=?");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// to return singleton object
	public static ToDoDAO getInstance() {
		if(toDoDAO==null)
			toDoDAO=new ToDoDAOImpl();
		return toDoDAO;
	}
	
	@Override
	public int register(Register register) {
		int regid=0;
		try {
			// pk generation
			rs=stmt.
			executeQuery("select max(regid) from register");
			if(rs.next()) {
				regid=rs.getInt(1);
			}
			regid++;
			
			
			// record insertion
			pstmt1.setInt(1, regid);
			pstmt1.setString(2, register.getFname());
			pstmt1.setString(3, register.getLname());
			pstmt1.setString(4, register.getEmail());
			pstmt1.setString(5, register.getPass());
			pstmt1.setLong(6, register.getMobile());
			pstmt1.setString(7, register.getAddress());
			int i=pstmt1.executeUpdate();
			if(i==1)
				System.out.println(
				"Record inserted into register table");
		} catch(Exception e) {
			e.printStackTrace();
		}
		// return statement
		return regid;
	}

	@Override
	public int login(String email, String pass) {
		int regId=0;
		try {
			rs=stmt.executeQuery("select regid from register where email='"+email+"' and pass='"+pass+"'");
			if(rs.next()) {
				regId=rs.getInt(1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return regId;
	}

	@Override
	public boolean addTask(Task task, int regid) {
		int taskId=0;
		boolean isNew=true;
		boolean flag=true;
		int i,j=0;
		try {
			rs=stmt.executeQuery("select taskid from taskid_pks where regid="+regid);
			if(rs.next()) {
				isNew=false;
				taskId=rs.getInt(1);
			}
			taskId++;
			
			con.setAutoCommit(false);
			pstmt2.setInt(1, taskId);
			pstmt2.setString(2, task.getTaskName());
			pstmt2.setString(3, task.getTaskDate());
			pstmt2.setInt(4, task.getTaskStatus());
			pstmt2.setInt(5, task.getTaskregid());
			i=pstmt2.executeUpdate();
			
			if(isNew) {
				pstmt3.setInt(1, task.getTaskregid());
				pstmt3.setInt(2, taskId);
				j=pstmt3.executeUpdate();
			} else {
				pstmt4.setInt(1, taskId);
				pstmt4.setInt(2, task.getTaskregid());
				j=pstmt4.executeUpdate();
			}
			
			if(i==1 && j==1) {
				con.commit();
				flag=true;
				System.out.println("TX Success, task added");
			} else {
				con.rollback();
				flag=false;
				System.out.println("TX Failed");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public List<Task> findAllTasksByRegId(int regid) {
		List<Task> taskList=new ArrayList<Task>();
		try {
			rs=stmt.executeQuery("select * from tasks where regid="+regid);
			while(rs.next()) {
				Task task=new Task(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5));
				taskList.add(task);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return taskList;
	}

	@Override
	public boolean markTaskCompleted(int regid, int taskid) {
		boolean flag=false;
		try {
			int i=stmt.executeUpdate("update tasks set taskstatus=3 where regid="+regid+" and taskid="+taskid);
			if(i==1) {
				flag=true;
				System.out.println("Task "+taskid+" of "+regid+" completed");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	 public String getFLNameByRegID(int regId) {
		    String flname="";
		    try {
		      rs=stmt.executeQuery("select fname,lname from register where regId="+regId);
		      if(rs.next()) {
		        String fname=rs.getString(1);
		        String lname=rs.getString(2);
		        flname=fname+" "+lname;
		      }
		    } catch(Exception e) {
		      e.printStackTrace();;
		    }
		    return flname;
		  }
		}
