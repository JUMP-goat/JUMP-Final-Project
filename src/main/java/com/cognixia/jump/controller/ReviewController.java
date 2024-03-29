package com.cognixia.jump.controller;

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
import com.cognixia.jump.repository.ReviewRepo;
import com.cognixia.jump.repository.UserRepo;

import io.swagger.annotations.ApiOperation;

import io.swagger.annotations.ApiOperation;

@RequestMapping("/api")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

	@Autowired
	ReviewRepo reviewRepo;

	@Autowired
	RestaurantController restaurantController;

	@Autowired
	UserController userController;

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/reviews")
	@ApiOperation(value = "Find all reviews", notes = "Get all reviews", response = Review.class)
	public ResponseEntity<List<Review>> getReviews() {
		return ResponseEntity.status(HttpStatus.OK).body(reviewRepo.findAll());
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/reviews/id/{review_id}")
	@ApiOperation(value = "Find a review by its id", notes = "Return the review", response = Review.class)
	public ResponseEntity<Review> getReviewById(@Valid @PathVariable("review_id") Long review_id)
			throws ResourceNotFoundException {
		if (!reviewRepo.existsById(review_id)) {
			throw new ResourceNotFoundException("Review with id " + review_id + " not found");
		}

		Review review = reviewRepo.findById(review_id).get();

		return ResponseEntity.status(HttpStatus.OK).body(review);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/user/id/{userId}/reviews")
	public ResponseEntity<List<Review>> getUserReviewsListById(@PathVariable long userId)
			throws ResourceNotFoundException {
		User user = userController.getUserById(userId).getBody();
		List<Review> reviews = user.getReviews();
		return ResponseEntity.status(HttpStatus.OK).body(reviews);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/user/id/{userId}/review")
	public ResponseEntity<Review> getUserReviewById(@PathVariable long userId, @RequestParam long id)
			throws ResourceNotFoundException {
		User user = userController.getUserById(userId).getBody(); // have this function handle the exceptions
		List<Review> reviewlist = user.getReviews();

		// iterate through list until id match is found

		for (Review r : reviewlist) {
			if (r.getReviewId() == id) {
				return ResponseEntity.status(HttpStatus.OK).body(r);
			}
		}
		// if id match wasn't found, throw exception
		throw new ResourceNotFoundException("Review with id " + id + " not found.");
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/restaurant/id/{restaurantId}/reviews")
	public ResponseEntity<List<Review>> getRestaurantsReviewsListById(@PathVariable long restaurantId)
			throws ResourceNotFoundException {
		Restaurant restaurant = restaurantController.getRestaurantById(restaurantId).getBody();
		List<Review> reviews = restaurant.getReviews();
		return ResponseEntity.status(HttpStatus.OK).body(reviews);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/restaurant/id/{restaurantId}/review")
	public ResponseEntity<Review> getRestaurantReviewById(@PathVariable long restaurantId, @RequestParam long id)
			throws ResourceNotFoundException {
		Restaurant restaurant = restaurantController.getRestaurantById(restaurantId).getBody(); // have this function
																								// handle the exceptions

		List<Review> reviewlist = restaurant.getReviews();

		// iterate through list until id match is found

		for (Review r : reviewlist) {
			if (r.getReviewId() == id) {
				return ResponseEntity.status(HttpStatus.OK).body(r);
			}
		}
		// if id match wasn't found, throw exception
		throw new ResourceNotFoundException("Review with id " + id + " not found.");
	}


	@CrossOrigin(origins = "http://localhost:3000")
	@PostMapping("/restaurants/id/{restaurantId}/reviews")
	public ResponseEntity<Review> addRestaurantReview(@PathVariable long restaurantId,
			@Valid @RequestBody Review review, @RequestParam long userId) throws ResourceNotFoundException {
		review.setReviewId(-1L);
		Restaurant restaurant = (Restaurant) restaurantController.getRestaurantById(restaurantId).getBody();
		User user = (User) userController.getUserById(userId).getBody();
		
		review.setRestaurant(restaurant);
		review.setUser(user);
		
		Review newReview = reviewRepo.save(review);

		recalculateRestaurantRating(restaurantId);

		return ResponseEntity.status(HttpStatus.CREATED).body(newReview);
	}

	// HELPER FUNCTION
	private void recalculateRestaurantRating(Long id) throws ResourceNotFoundException {
		List<Review> reviews = getRestaurantsReviewsListById(id).getBody();
		double sum = 0;
		for (Review r : reviews) {
			sum += r.getRating();
		}
		double avg = sum / reviews.size();

		Restaurant restaurant = (Restaurant) restaurantController.getRestaurantById(id).getBody();
		restaurant.setRating(avg);
		restaurantController.updateRestaurant(restaurant);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@DeleteMapping("/user/id/{userId}/review")
	@ApiOperation(value = "Delete User review by id", notes = "Delete user review by id (if exists)", response = Review.class)
	public ResponseEntity<Review> deleteUserReviewById(@PathVariable long userId, @RequestParam int id)
			throws ResourceNotFoundException {
		Review deleted = (Review) getUserReviewById(userId, id).getBody();
		reviewRepo.deleteReview(deleted.getReviewId());
		
		recalculateRestaurantRating(deleted.getRestaurant().getId());

		return ResponseEntity.status(HttpStatus.OK).body(deleted);
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@PutMapping("/user/id/{userId}/review")
	@ApiOperation(value = "Update User review by id", notes = "Update user review by id (if exists)", response = Review.class)
	public ResponseEntity<Review> updateUserReviewById(@PathVariable long userId, @Valid @RequestBody Review review)
			throws ResourceNotFoundException {
		Review reviewObj = getReviewById(review.getReviewId()).getBody();
		Restaurant restaurant = reviewObj.getRestaurant();
		review.setRestaurant(restaurant);
		
		User user = reviewObj.getUser();
		review.setUser(user);
		
		Review updated = (Review) getUserReviewById(userId, review.getReviewId()).getBody();
		reviewRepo.save(review);
		
		recalculateRestaurantRating(updated.getRestaurant().getId());
		
		return ResponseEntity.status(HttpStatus.OK).body(review);
	}
}
