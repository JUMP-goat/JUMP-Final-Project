package com.cognixia.jump.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cognixia.jump.exception.InvalidInputException;
import com.cognixia.jump.exception.ResourceNotFoundException;
import com.cognixia.jump.model.Restaurant;
import com.cognixia.jump.model.Review;
import com.cognixia.jump.model.User;
import com.cognixia.jump.repository.RestaurantRepo;

import io.swagger.annotations.ApiOperation;

@RequestMapping("/api")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantController {

	@Autowired
	RestaurantRepo restaurantRepo;

	// READ
	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/restaurants")
	@ApiOperation(value = "Find all restaurants", notes = "Get all restaurant names", response = Restaurant.class)
	public ResponseEntity<List<Restaurant>> getAllRestaurants() {
		List<Restaurant> list = restaurantRepo.findAll();

		return ResponseEntity.status(HttpStatus.OK).body(list);
	}

	// READ
	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/restaurants/id/{restaurant_id}")
	@ApiOperation(value = "Find restaurant by its id", notes = "Return the restaurant", response = Restaurant.class)
	public ResponseEntity<Restaurant> getRestaurantById(@Valid @PathVariable("restaurant_id") Long restaurant_id)
			throws ResourceNotFoundException {
		if (!restaurantRepo.existsById(restaurant_id)) {
			throw new ResourceNotFoundException("Restaurant with id " + restaurant_id + " not found");
		}

		Restaurant restaurant = restaurantRepo.findById(restaurant_id).get();

		return ResponseEntity.status(HttpStatus.OK).body(restaurant);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/restaurants/name/{name}")
	public ResponseEntity<Restaurant> getRestaurantByName(@Valid @PathVariable("name") String name)
			throws ResourceNotFoundException {
		if (!restaurantRepo.existsByRestaurantName(name)) {
			throw new ResourceNotFoundException("Restaurant with name " + name + " not found");
		}

		Restaurant restaurant = restaurantRepo.findByRestaurantName(name).get();

		return ResponseEntity.status(HttpStatus.OK).body(restaurant);
	}

	// CREATE
	@CrossOrigin(origins = "http://localhost:3000")
	@PostMapping("/restaurants")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Restaurant> addRestaurant(@Valid @RequestBody Restaurant restaurant) throws Exception {
		restaurant.setId(-1L);
		restaurant.setReviews(new ArrayList<Review>());
		restaurant.setRating(0.0);

		Restaurant added = restaurantRepo.save(restaurant);

		return ResponseEntity.status(HttpStatus.CREATED).body(added);
	}

	// UPDATE
	@CrossOrigin(origins = "http://localhost:3000")
	@PutMapping("/update/restaurants")
	@ApiOperation(value = "Update a restaurant", notes = "Restaurant to be updated", response = Restaurant.class)
	public ResponseEntity<Restaurant> updateRestaurant(@Valid @RequestBody Restaurant restaurant)
			throws ResourceNotFoundException {

		Optional<Restaurant> found = restaurantRepo.findById(restaurant.getId());
		restaurant.setReviews(new ArrayList<Review>());

		if (found.isPresent()) {
			Restaurant updated = restaurantRepo.save(restaurant);
			return ResponseEntity.status(HttpStatus.OK).body(updated);
		} else {
			throw new ResourceNotFoundException("Restaurant with id " + restaurant.getId() + " not found");
		}

	}

	// DELETE
	@CrossOrigin(origins = "http://localhost:3000")
	@DeleteMapping("/restaurants/id/{restaurant_id}")
	@ApiOperation(value = "Delete a restaurant by id", notes = "Delete restaurant", response = User.class)
	public ResponseEntity<Restaurant> deleteRestaurantById(@Valid @PathVariable Long restaurant_id)
			throws ResourceNotFoundException {

		Optional<Restaurant> restaurant = restaurantRepo.findById(restaurant_id);

		if (restaurant.isPresent()) {
			restaurantRepo.deleteById(restaurant_id);
			return ResponseEntity.status(HttpStatus.OK).body(restaurant.get());
		}

		else {
			throw new ResourceNotFoundException("Restaurant with id " + restaurant_id + " not found");
		}

	}

}
