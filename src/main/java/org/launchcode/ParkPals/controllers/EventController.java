package org.launchcode.ParkPals.controllers;
 

import org.launchcode.ParkPals.data.DogRepository;
import org.launchcode.ParkPals.data.EventRepository;
import org.launchcode.ParkPals.data.ParkRepository;
import org.launchcode.ParkPals.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class EventController {

    @Autowired
    AuthenticationController authenticationController;

    @Autowired
    ParkRepository parkRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    DogRepository dogRepository;

    @GetMapping("create-event/{placeId}/details")
    public String displayCreateEventForm(@PathVariable String placeId, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = authenticationController.getUserFromSession(session);
        Park park = parkRepository.findByPlaceId(placeId);
        List<Dog> attendingDogs = new ArrayList<>();
        model.addAttribute(new Event());
        model.addAttribute(placeId);
        model.addAttribute("types", DogTemperament.values());
        model.addAttribute("activityLevels", DogActivity.values());
        model.addAttribute("user", user);
        model.addAttribute("park", park);
        model.addAttribute("attendingDogs", attendingDogs);
        return "event/create-event";
    }

    @PostMapping("create-event/{placeId}/details")
    public String processCreateEventForm(@ModelAttribute @Valid Event event, @PathVariable String placeId, @RequestParam(required = false) int[] dogAttendees,
                                         Errors errors, Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = authenticationController.getUserFromSession(session);
        Park park = parkRepository.findByPlaceId(placeId);

        if(errors.hasErrors()) {
            model.addAttribute("types", DogTemperament.values());
            model.addAttribute("activityLevels", DogActivity.values());
            model.addAttribute("user", user);
            model.addAttribute("park", park);
            return "event/create-event";
        }

        if (dogAttendees != null) {
            for (int id : dogAttendees) {
                Optional<Dog> optionalDog = dogRepository.findById(id);
                Dog dog = optionalDog.get();
                dog.addEvents(event);
                event.addDogAttendee(dog);
            }
        }

        user.addEvents(event);
        park.addEvents(event);
        event.setPark(park);
        event.setCreator(user);
        eventRepository.save(event);
        return "redirect:/user/{id}/home";
    }

    @GetMapping("/event/{eventId}")
    public String viewEventProfileById(@PathVariable Integer eventId, Model model) {
        Optional optEvent = eventRepository.findById(eventId);
        if (optEvent.isPresent()) {
            Event event = (Event) optEvent.get();
            User creator = event.getCreator();
            model.addAttribute("event", event);
            model.addAttribute("user", creator);
            return "event/event-profile";
        } else {
            return "redirect:../";
        }

    }
}
