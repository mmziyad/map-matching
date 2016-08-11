package it4bi.ulb.barefoot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherCandidate;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.road.Configuration;
import com.bmwcarit.barefoot.road.PostGISReader;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.bmwcarit.barefoot.util.Tuple;
import com.esri.core.geometry.Point;

public class MapMatchingCore {

	public void processMapMatching(int input_type, String input_file, String config_file) {
		// TODO Auto-generated method stub

		// Create Configuration

		Map<Short, Tuple<Double, Integer>> config = null;
		try {
			config = getConfiguration(config_file);
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Load and construct road map
		RoadMap map = createRoadMap(config);

		// Instantiate matcher and state data structure
		Matcher matcher = new Matcher(map, new Dijkstra<Road, RoadPoint>(), new TimePriority(), new Geography());
		MatcherKState state = new MatcherKState();

		// Input as sample batch (offline) or sample stream (online)
		List<MatcherSample> samples = new LinkedList<MatcherSample>();

		try {
			mapMatchOnline(matcher, state, samples, input_file, input_type);
		} catch (IOException | ParseException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Offline map matching results
		// List<MatcherCandidate> sequence = state.sequence();
		// most likely sequence of positions
	}

	private static Map<Short, Tuple<Double, Integer>> getConfiguration(String config_file) throws JSONException, IOException {
		Map<Short, Tuple<Double, Integer>> config = Configuration.read(config_file);
		return config;
	}

	private static RoadMap createRoadMap(Map<Short, Tuple<Double, Integer>> config) {
		RoadMap map = RoadMap.Load(new PostGISReader(Constants.HOSTNAME, 
													 Constants.PORT, 
													 Constants.DB, 
													 Constants.TABLE, 
													 Constants.USERNAME, 
													 Constants.PASSWORD, 
													 config));
		map.construct();
		return map;
	}

	private static void mapMatchOnline(Matcher matcher, MatcherKState state, List<MatcherSample> samples, String input_file, int input_type)
			throws IOException, ParseException, FileNotFoundException, JSONException {

		if (input_type == 1) {
			JSONParser parser = new JSONParser();
			JSONArray GPSPositions = (JSONArray) parser.parse(new FileReader(input_file));

			for (int n = 0; n < GPSPositions.size(); n++) {
				JSONObject position = (JSONObject) GPSPositions.get(n);
				samples.add(new MatcherSample(new org.json.JSONObject(position.toJSONString())));

			}
		} else if (input_type == 2) {

			String line = "";

			BufferedReader br = new BufferedReader(new FileReader(input_file));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] elements = line.split(Constants.COMMA_STRING);

				Point current_point = new Point(Double.parseDouble(elements[1]), Double.parseDouble(elements[2]));
				long current_time = Long.parseLong(elements[0]);
				samples.add(new MatcherSample(current_time, current_point));

			}

		}

		// Iterative map matching of sample batch (offline) or sample stream
		// (online)
		for (MatcherSample sample : samples) {
			Set<MatcherCandidate> vector = matcher.execute(state.vector(), state.sample(), sample);
			state.update(vector, sample);

			// Online map matching result
			MatcherCandidate estimate = state.estimate(); // most likely
															// position estimate

			// if (estimate != null ){
			long id = estimate.point().edge().id(); // road id
			System.out.println("Time = " + sample.time() + 
								" X = " + sample.point().getX() + 
								" Y = " + sample.point().getY()  + 
								" INTERNAL ROAD ID = " + id );
			// }else{
			// System.out.println("No matches found");
			// }

			// Note: Check if the below code is required

			/*
			 * Point position = estimate.point().geometry(); // position
			 * MatcherTransition transition = estimate.transition(); if
			 * (transition != null) { // first point will have a null transition
			 * Route route = transition.route(); // route to position }
			 */
		}
	}
}
