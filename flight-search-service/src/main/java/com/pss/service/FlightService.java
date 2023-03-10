package com.pss.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pss.entity.AddFlightRequest;
import com.pss.entity.AdminSearchRequest;
import com.pss.entity.Flight;
import com.pss.entity.SearchRequest;
import com.pss.entity.SearchResponse;
import com.pss.repo.FlightRepo;

@Service
public class FlightService {
	// Function to calculate distance between two airports
	public static double distance(double lat1, double lat2, double lon1, double lon2) {

		// The math module contains a function
		// named toRadians which converts from
		// degrees to radians.
		lon1 = Math.toRadians(lon1);
		lon2 = Math.toRadians(lon2);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// Haversine formula
		double dlon = lon2 - lon1;
		double dlat = lat2 - lat1;
		double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

		double c = 2 * Math.asin(Math.sqrt(a));

		// Radius of earth in kilometers. Use 3956
		// for miles
		double r = 6371;

		// calculate the result
		return (c * r);
	}

	@Autowired
	private FlightRepo flightRepo;

	@Autowired
	private AirportService airportService;

	@Autowired
	RestTemplate restTemplate;

	// Admin Service
	// Add Flight
	public Flight addFlight(AddFlightRequest newFlightRequest) {
		System.out.println("Add flight");
		Flight newFlight = new Flight();

		newFlight.setFlightId(0L);

		newFlight.setSource(airportService.getAirportByCode(newFlightRequest.getSourceCode()).get());

		newFlight.setDestination(airportService.getAirportByCode(newFlightRequest.getDestinationCode()).get());

		// Calculate the distance between two airports
		newFlight.setDistance(distance(newFlight.getSource().getLat(), newFlight.getDestination().getLat(),
				newFlight.getSource().getLon(), newFlight.getDestination().getLon()));

		// Set DepartureTime
		newFlight.setDepartureTime(LocalTime.parse(newFlightRequest.getTimeOfDeparture(), DateTimeFormatter.ISO_TIME));

		// Set ArrivalTime
		newFlight.setArrivalTime(LocalTime.parse(newFlightRequest.getTimeOfArrival(), DateTimeFormatter.ISO_TIME));

		newFlight.setFlightStatus(true);
		;

		return flightRepo.saveAndFlush(newFlight);
	}

	// Admin Service
	// Get flight by flightId
	public Optional<Flight> getFlightById(Long flightId) {
		if (!flightRepo.existsById(flightId)) {
			throw new RuntimeException("Flight with flight Id : " + flightId + " is not found.");
		}

		return flightRepo.findById(flightId);
	}

	// Admin Service
	// Get all flight data
	public List<Flight> getAllFlight() {
		System.out.println(LocalDateTime.now() +"All Flights");
		return flightRepo.findAll();
	}

	// Admin Service
	// Delete flight by flight Id
	public String removeFlight(Long flightId) {
		if (!flightRepo.existsById(flightId)) {
			throw new RuntimeException("Flight with flight Id : " + flightId + " is not found.");
		}

		flightRepo.deleteById(flightId);
		return "Flight removed successfully";
	}

	// Admin Service
	// Edit Flight
	public Flight editFlight(Long flightId, AddFlightRequest updatedFlight) {
		if (!flightRepo.existsById(flightId)) {
			throw new RuntimeException("Flight with flight Id : " + flightId + " is not found.");
		}

		Flight flightToUpdate = flightRepo.findById(flightId).get();

		flightToUpdate.setSource(airportService.getAirportByCode(updatedFlight.getSourceCode()).get());

		flightToUpdate.setDestination(airportService.getAirportByCode(updatedFlight.getDestinationCode()).get());

		// Calculate distance between two airports
		flightToUpdate
				.setDistance(distance(flightToUpdate.getSource().getLat(), flightToUpdate.getDestination().getLat(),
						flightToUpdate.getSource().getLon(), flightToUpdate.getDestination().getLon()));

		// Set DepartureTime
		flightToUpdate
				.setDepartureTime(LocalTime.parse(updatedFlight.getTimeOfDeparture(), DateTimeFormatter.ISO_TIME));

		// Set ArrivalTime
		flightToUpdate.setArrivalTime(LocalTime.parse(updatedFlight.getTimeOfArrival(), DateTimeFormatter.ISO_TIME));

		flightToUpdate.setFlightStatus(updatedFlight.getFlightStatus());

		return flightRepo.saveAndFlush(flightToUpdate);

	}

	// Get flight which depart in morning
	public List<Flight> getMorningFlights(List<Flight> flights) {
		if (flights == null) {
			flights = flightRepo.findAll();
		}
		System.out.println(flights);
		return flights.stream().filter(flight -> flight.getDepartureTime().isBefore(LocalTime.NOON)).toList();

	}

	// Get flight which depart in afternoon
	public List<Flight> getAfternoonFlights(List<Flight> flights) {

		if (flights == null) {
			flights = flightRepo.findAll();
		}

		return flights.stream().filter(flight -> flight.getDepartureTime().isAfter(LocalTime.NOON)
				&& flight.getDepartureTime().isBefore(LocalTime.of(18, 00))).toList();
	}

	// Get flight which depart in night
	public List<Flight> getNightFlights(List<Flight> flights) {

		if (flights == null) {
			flights = flightRepo.findAll();
		}

		return flights.stream().filter(flight -> flight.getDepartureTime().isAfter(LocalTime.of(18, 00))
				&& flight.getDepartureTime().isBefore(LocalTime.of(23, 59, 59))).toList();
	}

	// Get flight based on source and destination
	public List<Flight> getFlightBySourceAndDestination(String source, String destination) {
		return flightRepo.findBySourceAndDestination(source, destination);
	}

	// Get flight by source, destination, date and no. of passenger
	public List<SearchResponse> getFlightsForUser(String source, String destination, LocalDate date,
			Integer noOfPassenger) {
		return flightRepo.findBySourceAndDestination(source, destination).stream().filter((flight) -> {

			Integer passengerCount = restTemplate
					.getForEntity("http://booking-service/booking/getPassengerCount/" + flight.getFlightId() + "/" + date,
							Integer.class)
					.getBody();

			if (passengerCount == null) {
				passengerCount = 0;
			}
			return (passengerCount + noOfPassenger <= 120) && flight.getFlightStatus();

		}).map((flight) -> {

			Integer fare = restTemplate
					.getForEntity("http://fare-service/getFare/" + flight.getFlightId() + "/" + date, Integer.class)
					.getBody();

			return new SearchResponse(flight, fare);

		}).toList();
	}

	// user search
	public Map<String, List<SearchResponse>> searchFlight(SearchRequest searchRequest) {

		Map<String, List<SearchResponse>> searchResult = new HashMap<String, List<SearchResponse>>();

		searchResult.put("flights", getFlightsForUser(searchRequest.getSource(), searchRequest.getDestination(),
				searchRequest.getDateOfTravelling(), searchRequest.getNoOfPassenger()));

		if (searchRequest.getDateOfReturn() != null) {
			searchResult.put("returnFlights", getFlightsForUser(searchRequest.getDestination(),
					searchRequest.getSource(), searchRequest.getDateOfReturn(), searchRequest.getNoOfPassenger()));
		} else {
			searchResult.put("returnFlights", null);
		}

		return searchResult;

	}

	// Admin Search
	public List<Flight> adminSearch(AdminSearchRequest req) {

		System.out.println("Request = " + req);
		if (req.getFlightId() != null) {
			List<Flight> flights = new ArrayList<Flight>();
			flights.add(getFlightById(req.getFlightId()).get());
			return flights;
		}

		if (req.getSource() != null && req.getDestination() != null && !req.getSource().equals("")
				&& !req.getDestination().equals("")) {
			List<Flight> flights = getFlightBySourceAndDestination(req.getSource(), req.getDestination());
			if (req.getTime() != null && !req.getTime().equals("")) {

				if (req.getTime().equals("Morning")) {
					return getMorningFlights(flights);
				} else if (req.getTime().equals("Afternoon")) {
					return getAfternoonFlights(flights);
				} else {
					return getNightFlights(flights);
				}
			} else {
				return flights;
			}
		}

		if (req.getTime() != null) {

			if (req.getTime().equals("Morning")) {
				return getMorningFlights(null);
			}

			if (req.getTime().equals("Afternoon")) {
				return getAfternoonFlights(null);
			}

			if (req.getTime().equals("Night"))
				return getNightFlights(null);
		}

		return getAllFlight();
	}

	// Admin can change flight status
	public String changeFlightStatus(Long flightId) {
		if (!flightRepo.existsById(flightId)) {
			throw new RuntimeException("Flight with flight Id : " + flightId + " is not found.");
		}

		Flight flight = flightRepo.findById(flightId).get();
		flight.setFlightStatus(!flight.getFlightStatus());
		flightRepo.saveAndFlush(flight);
		if (!flight.getFlightStatus())
			return "Flight is now Enable";

		return "Flight is now Disable";
	}
}
