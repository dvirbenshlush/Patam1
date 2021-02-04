package test;

import java.util.ArrayList;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;
	
	public CLI(DefaultIO dio) {
		this.dio=dio;
		c=new Commands(dio); 
		commands=new ArrayList<>();
		commands.add(c.new mainCommand());
		commands.add(c.new uploadCommand());
		commands.add(c.new algorithmCommand());
		commands.add(c.new detectCommand());
		commands.add(c.new displayCommand());
		commands.add(c.new analyzeCommand());
		commands.add(c.new exitCommand());

		// implement
	}
	
	public void start() {
		// implement
		commands.get(0).execute();
	}
}
