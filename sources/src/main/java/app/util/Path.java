package app.util;

import app.util.Path.Web;
import lombok.*;

public class Path {

	public static class Web {
		@Getter public static final String HOME = "/home/";
		@Getter public static final String SIGNIN = "/signin/";
		@Getter public static final String SIGNUP = "/signup/";
		@Getter public static final String SIGNOUT = "/signout/";
		@Getter public static final String PROFILE = "/profile/";
		@Getter public static final String STOCKDATA = "/stockdata/";
		 @Getter public static final String STOCKS = "/stocks/";
	}
	
	public static class Templates {
		public static final String HOME = "/velocity/home/home.vm";
		public static final String SIGNIN = "/velocity/signin/signin.vm";
		public static final String SIGNUP = "/velocity/signup/signup.vm";
		public static final String NOT_FOUND = "/velocity/notFound.vm";
	}
}
