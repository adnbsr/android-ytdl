package com.andromedalabs.ytdl.model;

public class VideoInfo {

	public String token;
	public String min_version;
	public Args args;
	
	public VideoInfo(){}
	
	
	public class Args{
		
		public String url_encoded_fmt_stream_map;
		public String adaptive_fmts;

		
	}

}
