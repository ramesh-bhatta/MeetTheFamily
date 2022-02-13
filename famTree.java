
import java.util.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
class famTree {
	private ArrayList<Generation> genTree; // overall tree that holds the list of generations
	private Queue queue; // queue which temporarily holds persons which have not yet been added to the
							// overall family tree
	private BufferedReader br;

	public famTree() {
		genTree = new ArrayList<Generation>();
		queue = new Queue();
	}

	private void addChildren(int i, String parent) { // adds children nodes whose parent matches the input string
		if (genTree.size() > i + 1 && genTree.get(i + 1).areChildren(genTree.get(i).nodes)) {
			genTree.get(i + 1).nodes.addAll(queue.dequeue(parent));
		} else if (genTree.size() > i + 1 && !genTree.get(i + 1).areChildren(genTree.get(i).nodes)) {
			Generation gen = new Generation(queue.dequeue(parent));
			genTree.add(i + 1, gen);
		} else {
			Generation gen = new Generation(queue.dequeue(parent));
			genTree.add(gen);
		}
	}

	public void addChildWithMotherName(String childName, String motherName, Boolean gender, String spouse) {

		for (int i = 0; i < genTree.size(); i++) {
			for (Person person : genTree.get(i).nodes) {
				// if((person.name.equals(motherName) && !person.isMale) ||
				// (person.spouseName.equals(motherName) && person.isMale) ){
				if ((person.name.equals(motherName) && !person.isMale)
						|| (person.spouseName == motherName && person.isMale)) {

					String parentName = motherName;
					if (person.isMale) {
						parentName = person.name;
					}

					// if child is added as new generation
					if (genTree.size() - 1 == i) {
						Generation gen = new Generation(childName, parentName, gender, spouse);
						genTree.add(gen);
					} else {
						Person p = new Person(childName, parentName, gender, spouse);
						genTree.get(i + 1).nodes.add(p);
					}

				}
			}
		}
	}

	public void getRelationship(String name, String relationshipType) {

		Boolean personExists = false;
		Integer personGen = 0;
		for (int i = 0; i < genTree.size(); i++) {
			for (Person person : genTree.get(i).nodes) {
				if (person.name.equals(name) || person.spouseName == name) {
					personExists = true;
					personGen = i;
					break;
				}
			}
			if (personExists) {
				break;
			}
		}


		if (!personExists) {
			System.out.println("Sorry, person not found");
		} else {
			if (relationshipType.equals("Sibblings") ) {
				System.out.println("Person Found "+ personExists);
				System.out.println("--------------------");
				System.out.println("Sibblings of " + name);
				System.out.println("--------------------");
				for (Person person : genTree.get(personGen).nodes) {
					if(person.name.equals(name)) continue;
					System.out.print(person.name + " ");
				}
			}
		}

	}

	public void testInput(String filename) {
		String line;
		try {
			br = new BufferedReader(new FileReader(filename));
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] names = line.split(" ");
				switch (names[0]) {
					case "ADD_CHILD":
						String motherName = names[1];
						String childName = names[2];
						Boolean isMale = names[3] == "Female" ? false : true;
						this.addChildWithMotherName(childName, motherName, isMale, null);
						break;
					case "GET_RELATIONSHIP":
						String name = names[1];
						String relType = names[2];
						this.getRelationship(name, relType);
						break;
					default:
						System.exit(0);
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void fillTree(String filename) {

		String line;
		try {
			br = new BufferedReader(new FileReader(filename));
			br.readLine();
			while ((line = br.readLine()) != null) {

				String[] names = line.split(",");
				Boolean isMale = Boolean.parseBoolean(names[2]);
				String spouseName = names[3].equals("NULL") ? null : names[3];

				// fill the first read person into the tree regardless of whether it is the root
				// or not.
				boolean found = false;
				if (genTree.size() == 0 || names[0].equals("NULL")) {
					Generation gen = new Generation(names[1], names[0], isMale, spouseName);
					genTree.add(0, gen);
					found = true;
				} else {
					for (int i = 0; !found && i < genTree.size(); i++) {
						for (Person person : genTree.get(i).nodes) { // check if the read person is a parent/child of
																		// any person in the tree (check generation
																		// after generation)
							if (names[1].equals(person.parent) && i == 0) { // check if it is a parent
								Generation gen = new Generation(names[1], names[0], isMale, spouseName);
								genTree.add(0, gen);
								found = true;
								break;
							} else if (names[1].equals(person.parent) && i > 0
									&& genTree.get(i).areChildren(genTree.get(i - 1).nodes)) {
								genTree.get(i - 1).addPerson(names[1], names[0], isMale, spouseName);
								found = true;
								break;
							} else if (names[1].equals(person.parent) && i > 0
									&& !genTree.get(i).areChildren(genTree.get(i - 1).nodes)) {
								Generation gen = new Generation(names[1], names[0], isMale, spouseName);
								genTree.add(i - 1, gen);
								found = true;
								break;
							} else if (names[0].equals(person.name) && i + 1 == genTree.size()) { // now check if it is
																									// a child of any
																									// node
								Generation gen = new Generation(names[1], names[0], isMale, spouseName);
								genTree.add(gen);
								found = true;
								break;
							} else if (names[0].equals(person.name) && i + 1 < genTree.size()
									&& genTree.get(i + 1).areChildren(genTree.get(i).nodes)) {
								genTree.get(i + 1).addPerson(names[1], names[0], isMale, spouseName);
								found = true;
								break;
							} else if (names[0].equals(person.name) && i + 1 < genTree.size()
									&& genTree.get(i + 1).areChildren(genTree.get(i).nodes)) {
								Generation gen = new Generation(names[1], names[0], isMale, spouseName);
								genTree.add(i - 1, gen);
								found = true;
								break;
							}
						}
					}
				}
				if (!found) {
					queue.enqueue(names[1], names[0], isMale, spouseName);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// This part of the function searches through every node in the pre-filled tree
		// and checks whether it has any children stored in the p.queue
		for (int i = 0; i < genTree.size() && !queue.empty(); i++) {
			for (Person person : genTree.get(i).nodes) {
				if (queue.containsParent(person.name)) {
					this.addChildren(i, person.name);
				}
				if (queue.empty())
					break;
			}
		}
	}

	public void search(int depth) {
		ArrayList<Person> nodes = genTree.get(depth).nodes;
		for (Person person : nodes) {
			String gender = person.isMale ? "Male" : "Female";
			System.out.println("Name: " + person.name + " Gender: " + gender + " Spouse: " + person.spouseName);
		}
	}

	private class Queue {
		private ArrayList<Person> queue;
		private ArrayList<String> parents;

		public Queue() {
			queue = new ArrayList<Person>();
			parents = new ArrayList<String>();
		}

		public void enqueue(String name, String parent, Boolean isMale, String spouseName) { // adds new person into the
																								// priority queue
			queue.add(new Person(name, parent, isMale, spouseName));
			parents.add(new String(parent));
		}

		public boolean containsParent(String parent) {
			if (parents.contains(parent))
				return true;
			else
				return false;
		}

		public ArrayList<Person> dequeue(String parent) { // removes person(s) whose parent matches the input string and
															// adds it to the family tree
			ArrayList<Person> children = new ArrayList<Person>();
			for (Person person : this.queue) {
				if (person.parent.equals(parent)) {
					children.add(person);
				}
			}
			parents.remove(parent);
			return children;
		}

		public boolean empty() {
			if (queue.size() > 0)
				return false;
			else
				return true;
		}

	}

	// Class node representing a name and its parent
	private class Person {
		public String name;
		public String parent;
		public Boolean isMale;
		public String spouseName;

		public Person(String personName, String personParent, Boolean gender, String spouse) {
			name = personName;
			parent = personParent;
			isMale = gender;
			spouseName = spouse;
		}
	}

	private class Generation {
		public ArrayList<Person> nodes;

		public Generation() {
			nodes = new ArrayList<Person>();
		}

		public Generation(String name, String parent, Boolean isMale, String partner) {
			nodes = new ArrayList<Person>();
			nodes.add(0, new Person(name, parent, isMale, partner));
		}

		public Generation(ArrayList<Person> children) {
			nodes = new ArrayList<Person>();
			nodes.addAll(children);
		}

		public void addPerson(String name, String parent, Boolean isMale, String partner) {
			nodes.add(new Person(name, parent, isMale, partner));
		}

		public boolean areChildren(ArrayList<Person> parent) { // checks whether the current generation are the children
																// of the previous generation in the tree
			for (Person child : nodes) {
				for (Person par : parent) {
					if (child.parent.equals(par.name)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public static void main(String[] args) {
		if (args.length != 2)
			System.err.println("Enter Input Correctly");
		else {
			famTree tree = new famTree();
			Path currentRelativePath = Paths.get("");
			String projectDir = currentRelativePath.toAbsolutePath().toString();
			String membersFile = projectDir+"/" + args[0];
			tree.fillTree(membersFile);

			// tree.addChildWithMotherName("Rob", "Victoire", true,null);

			System.out.println("Family Members before adding child");
			for (int i = 0; i < tree.genTree.size(); i++) {
				System.out.println("----------------------------");
				System.out.println("Generation :" + i + " Family Members");
				System.out.println("----------------------------");
				tree.search(i);
			}

			String testFile = projectDir+"/"+ args[1];
			tree.testInput(testFile);

			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println("Family Members After Testing");
			for (int i = 0; i < tree.genTree.size(); i++) {
				System.out.println("----------------------------");
				System.out.println("Generation :" + i + " Family Members");
				System.out.println("----------------------------");
				tree.search(i);
			}

		}
	}
}