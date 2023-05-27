package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train=new Train();
        String route=generateRoute(trainEntryDto.getStationRoute());

        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        Train savedTrain=trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    private String generateRoute(List<Station> stationRoute) {
        StringBuilder sb=new StringBuilder();
        int n=stationRoute.size();
        for(int i=0;i<n-1;i++)
        {
            sb.append(stationRoute.get(i)+",");
        }
        sb.append(stationRoute.get(n-1));

        return sb.toString();

    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto)  {

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Optional<Train> optionalTrain=trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        Train train=optionalTrain.get();
        Station fromStation=seatAvailabilityEntryDto.getFromStation();
        Station toStation=seatAvailabilityEntryDto.getToStation();

        int totalSeats = train.getNoOfSeats();
        List<Ticket> bookedTickets = train.getBookedTickets();
        int bookedSeatsBetweenStations = 0;

        for (Ticket ticket : bookedTickets) {
            if (ticket.getFromStation().equals(fromStation) && ticket.getToStation().equals(toStation)) {
                // If the ticket is from the starting station to the ending station, consider all seats as booked
                bookedSeatsBetweenStations += ticket.getPassengersList().size();
            } else if (ticket.getFromStation().equals(fromStation)) {
                // If the ticket starts from the desired fromStation
                bookedSeatsBetweenStations += ticket.getPassengersList().size();
            } else if (ticket.getToStation().equals(toStation)) {
                // If the ticket ends at the desired toStation
                bookedSeatsBetweenStations += ticket.getPassengersList().size();
            } else if (ticket.getFromStation().compareTo(fromStation) > 0 && ticket.getToStation().compareTo(toStation) < 0) {
                // If the ticket falls completely within the range of the desired stations
                bookedSeatsBetweenStations += ticket.getPassengersList().size();
            }
        }

        return totalSeats - bookedSeatsBetweenStations;


    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Optional<Train> optionalTrain=trainRepository.findById(trainId);
        if(!optionalTrain.isPresent())
            throw new Exception("Train is not passing from this station");
        Train train=optionalTrain.get();


        if(!isPassingStation(train,station))
            throw new Exception("Train is not passing from this station");

        int n=getNumberOfPeopleFromStation(train,station);

        return n;
    }

    private int getNumberOfPeopleFromStation(Train train, Station station) {
        List<Ticket> ticketList=train.getBookedTickets();
        int n=0;
        for(Ticket ticket:ticketList)
        {
            if(ticket.getFromStation().equals(station))
                n+=ticket.getPassengersList().size();
        }
        return n;
    }

    private boolean isPassingStation(Train train, Station station) {
        String stations[]=train.getRoute().split(",");
        String name=station.name();
        for(int i=0;i<stations.length-1;i++)
        {
            if(stations[i].equals(name))
                return true;
        }
        return false;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) throws Exception {

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        int age=Integer.MIN_VALUE;
        Optional<Train> optionalTrain=trainRepository.findById(trainId);
        if(optionalTrain.isEmpty())
            throw new Exception("Train not fount");
        Train train=optionalTrain.get();
        List<Ticket> bookedTickets=train.getBookedTickets();

        for(Ticket ticket:bookedTickets)
        {
            List<Passenger> passengerList=ticket.getPassengersList();
            for(Passenger passenger:passengerList)
            {
               age= Math.max(passenger.getAge(),age);
            }
        }

        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train> trains=new ArrayList<>();

        List<Integer> trainIds=new ArrayList<>();
        for(Train train:trainRepository.findAll())
        {
            List<String> stations=new ArrayList<>(Arrays.asList(train.getRoute().split(",")));
            String desiredStation = station.name();
            int index = stations.indexOf(desiredStation);
            if(index!=-1)
            {
                LocalTime departureTime=train.getDepartureTime();
                LocalTime arrivalTime=departureTime.plusHours(index);
                if(arrivalTime.isAfter(startTime)&& arrivalTime.isBefore(endTime))
                {
                    trainIds.add(train.getTrainId());
                }
            }
        }

        return trainIds;
    }

}
