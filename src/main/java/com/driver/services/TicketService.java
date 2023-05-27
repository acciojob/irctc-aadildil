package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.assertj.core.api.OptionalAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{




        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        //checking for booking passenger
        Optional<Passenger> bookingPassengerOptional=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        if(bookingPassengerOptional.isEmpty())
            throw new Exception("passenger not found");

        //checking for train
        Optional<Train> optionalTrain=trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(optionalTrain.isEmpty())
            throw new Exception("train not found");

        //booking person
        Passenger bookingPassenger=bookingPassengerOptional.get();
        //booking train
        Train train=optionalTrain.get();
        //passengers list
        List<Passenger> passengerList=getPassengers(bookTicketEntryDto.getPassengerIds());

        //number of seats booked
        int numberOfSeatsNeeded=passengerList.size();

        int totalSeatsBooked=0;
        for(Ticket ticket:train.getBookedTickets())
        {
            totalSeatsBooked+=ticket.getPassengersList().size();
        }

        //checking for tickets availability
        if(train.getNoOfSeats()-totalSeatsBooked<numberOfSeatsNeeded)
            throw new Exception("Less tickets are available");




        //calculating fare
        int startStationNumber=getStationNo(train,bookTicketEntryDto.getFromStation());
        int endStationNumber=getStationNo(train,bookTicketEntryDto.getToStation());
        int numberOfStations=endStationNumber-startStationNumber;
        int totalFare=300*(numberOfStations)*numberOfSeatsNeeded;
        //train.setNoOfSeats(train.getNoOfSeats()-numberOfSeatsNeeded);


        Ticket ticket=new Ticket();
        ticket.setTotalFare(totalFare);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);


      // Save both ticket and train entities
       Ticket savedTicket= ticketRepository.save(ticket);
       train.getBookedTickets().add(savedTicket);
        trainRepository.save(train);
        for(Passenger passenger:passengerList)
        {
            passenger.getBookedTickets().add(savedTicket);
            passengerRepository.save(passenger);
        }








        return savedTicket.getTicketId();


    }

    private List<Passenger> getPassengers(List<Integer> passengerIds) throws Exception {
        List<Passenger> passengers=new ArrayList<>();
        for(Integer passId:passengerIds)
        {
            Optional<Passenger> passengerOptional=passengerRepository.findById(passId);
            if(passengerOptional.isEmpty())
                throw new Exception("passenger not found");
            else
                passengers.add(passengerOptional.get());
        }
        return passengers;
    }

    private int getStationNo(Train train, Station trainStation) {

        String stations[]=train.getRoute().split(",");
        String name=trainStation.name();
        for(int i=0;i<stations.length;i++)
        {
            if(stations[i].equals(name))
                return i;
        }
        return -1;
    }
}
