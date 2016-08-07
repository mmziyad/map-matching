package it4bi.ulb.barefoot;

public class MapMatchingDriver {

	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Incorrect number of Arguments");
		} else {

			int input_type = Integer.parseInt(args[0]);
			String input_file = args[1];
			String config_file = args[2];
			
			/*Example:
				input_type: '1' (For JSON input format)
				input file: '/barefoot-master/src/test/resources/com/bmwcarit/barefoot/matcher/x0001-015.json'
				config_file: '/barefoot-master/bfmap/road-types.json'
			*/
			MapMatchingCore core = new MapMatchingCore();
			core.processMapMatching(input_type, input_file, config_file);
		}
	}
}
