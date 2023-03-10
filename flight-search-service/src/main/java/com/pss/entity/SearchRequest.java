package com.pss.entity;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SearchRequest {

	@NotNull(message = "Source airport is required.")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Airport code should be 3 characters long and should have only captial letters")
	private String source;

	@NotNull(message = "Destination airport is required.")
	@Pattern(regexp = "^[A-Z]{3}$", message = "Airport code should be 3 characters long and should have only captial letters")
	private String destination;

	private LocalDate dateOfTravelling;

	private LocalDate dateOfReturn;

	@NotNull(message = "Passenger is required.")
	@Min(value = 1, message = "Minimum 1 passenger is required.")
	@Max(value = 10, message = "Maximum 10 passenger is allowed.")
	private Integer noOfPassenger;

	public SearchRequest() {
		super();
	}

	public SearchRequest(
			@NotNull(message = "Source airport is required.") @Pattern(regexp = "^[A-Z]{3}$", message = "Airport code should be 3 characters long and should have only captial letters") String source,
			@NotNull(message = "Destination airport is required.") @Pattern(regexp = "^[A-Z]{3}$", message = "Airport code should be 3 characters long and should have only captial letters") String destination,
			LocalDate dateOfTravelling, LocalDate dateOfReturn,
			@NotNull(message = "Passenger is required.") @Min(value = 1, message = "Minimum 1 passenger is required.") @Max(value = 10, message = "Maximum 10 passenger is allowed.") Integer noOfPassenger) {
		super();
		this.source = source;
		this.destination = destination;
		this.dateOfTravelling = dateOfTravelling;
		this.dateOfReturn = dateOfReturn;
		this.noOfPassenger = noOfPassenger;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public LocalDate getDateOfTravelling() {
		return dateOfTravelling;
	}

	public void setDateOfTravelling(LocalDate dateOfTravelling) {
		this.dateOfTravelling = dateOfTravelling;
	}

	public Integer getNoOfPassenger() {
		return noOfPassenger;
	}

	public void setNoOfPassenger(Integer noOfPassenger) {
		this.noOfPassenger = noOfPassenger;
	}

	public LocalDate getDateOfReturn() {
		return dateOfReturn;
	}

	public void setDateOfReturn(LocalDate dateOfReturn) {
		this.dateOfReturn = dateOfReturn;
	}

	@Override
	public String toString() {
		return "SearchRequest [source=" + source + ", destination=" + destination + ", dateOfTravelling="
				+ dateOfTravelling + ", dateOfReturn=" + dateOfReturn + ", noOfPassenger=" + noOfPassenger + "]";
	}

}
