package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;

	public CLI(DefaultIO dio) {
		this.dio = dio;
		c = new Commands(dio);
		commands = new ArrayList<>();
		commands.add(c.new mainCommand());
		commands.add(c.new uploadCommand());
		commands.add(c.new algoCommand());
		commands.add(c.new detectCommand());
		commands.add(c.new displayCommand());
		//commands.add(c.new downloadCommand());
		commands.add(c.new upAnalayzeCommand());
		commands.add(c.new exitCommand());
		// example: commands.add(c.new ExampleCommand());
		// implement
	}

	public void start() {
		commands.get(0).execute();
	}
}
