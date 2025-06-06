package com.eternalcoders.pointedge.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.eternalcoders.pointedge.dto.CustomerDTO;
import com.eternalcoders.pointedge.dto.LoyaltyThresholdsDTO;
import com.eternalcoders.pointedge.entity.Customer;
import com.eternalcoders.pointedge.entity.Customer.Tier;
import com.eternalcoders.pointedge.service.CustomerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@CrossOrigin
@RequestMapping(value = "api/v1/discount/customer")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    // get all customers
    @GetMapping("/get-all-customers")
    public List<CustomerDTO> getCustomersDetails() {
        return customerService.getAllCustomers();
    }
    
    // add customer
    @PostMapping("/add-customer")
    public CustomerDTO addCustomerDetails(@RequestBody CustomerDTO customerDTO) {
        return customerService.addCustomer(customerDTO);
    }

    // get customer by phone
    @GetMapping("/get-customer/{phone}")
    public CustomerDTO getCustomerById(@PathVariable String phone) {  
        return customerService.getCustomerById(phone);
    }

    //delete customer by phone
    @DeleteMapping("/delete-customer/{phone}")
    public ResponseEntity<String> deleteCustomer(@PathVariable String phone) {
        try {
            customerService.deleteByPhone(phone);
            return ResponseEntity.ok("Customer deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Customer not found: " + e.getMessage());
        }
    }

    // get customer count
    @GetMapping("/count")
    public ResponseEntity<Long> countCustomers() {
        long count = customerService.countCustomers();
        return ResponseEntity.ok(count);
    }
    
    // search customers
    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(@RequestParam String query) {
        List<CustomerDTO> customers = customerService.searchCustomers(query);
        return ResponseEntity.ok(customers);
    }

    // get customer by id
    @PutMapping("/update-customer/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        try {
            CustomerDTO updatedCustomer = customerService.updateCustomerById(id, customerDTO);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + e.getMessage(), e);
        }
    }    
    
    // get customer by phone
    @GetMapping("/get-customer-by-phone/{phone}")
    public ResponseEntity<Map<String, Object>> getCustomerByPhone(@PathVariable String phone) {
        CustomerDTO customer = customerService.getCustomerByPhoneNullable(phone);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", customer != null);
        response.put("customer", customer);
        return ResponseEntity.ok(response);
    }

    // update customer points
    @PatchMapping("/update-points/{phone}")
    public ResponseEntity<CustomerDTO> updateCustomerPoints(
            @PathVariable String phone,
            @RequestParam Double points) {  
        try {
            CustomerDTO updatedCustomer = customerService.updateCustomerPoints(phone, points);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + e.getMessage(), e);
        }
    }

    // update customer tier
    @PatchMapping("/update-tier/{phone}")
    public ResponseEntity<CustomerDTO> updateCustomerTier(
            @PathVariable String phone,
            @RequestParam Customer.Tier tier) {  // Using fully qualified enum name
        try {
            CustomerDTO updatedCustomer = customerService.updateCustomerTier(phone, tier);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + e.getMessage(), e);
        }
    }

    // Get customer count by tier
    @GetMapping("/count-by-tier")
    public ResponseEntity<Map<String, Long>> countCustomersByTier() {
        Map<Customer.Tier, Long> tierCounts = customerService.countCustomersByTier();
        Map<String, Long> response = new HashMap<>();
        tierCounts.forEach((tier, count) -> response.put(tier.name(), count));
        
        return ResponseEntity.ok(response);
    }

    // get tier by phone
    @GetMapping("/get-tier/{phone}")
    public ResponseEntity<Tier> getCustomerTier(@PathVariable String phone) {
        try {
            Tier tier = customerService.getCustomerTierByPhone(phone);
            return ResponseEntity.ok(tier);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + e.getMessage(), e);
        }
    }
    

    // fetch orders
    @GetMapping("/orders/grouped/{phone}")
    public ResponseEntity<List<Map<String, Object>>> getGroupedOrdersByPhone(@PathVariable String phone) {
        try {
            List<Map<String, Object>> groupedOrders = customerService.getOrderDetailsGroupedByOrderIdAndPhone(phone);
            return ResponseEntity.ok(groupedOrders);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Error retrieving orders: " + e.getMessage(), 
                e
            );
        }
    }

    // update customer tiers when update settings
    @GetMapping("/loyalty-thresholds2")
        public ResponseEntity<LoyaltyThresholdsDTO> getLoyaltyThresholds() {
            return ResponseEntity.ok(customerService.getLoyaltyThresholds());
        }

    // update all customer tiers
    @PatchMapping("/update-all-tiers")
    public ResponseEntity<Map<String, Object>> updateAllCustomerTiers() {
        try {
            customerService.updateAllCustomerTiers();
            
            Map<Customer.Tier, Long> tierCounts = customerService.countCustomersByTier();

            LoyaltyThresholdsDTO currentThresholds = customerService.getLoyaltyThresholds();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All customer tiers updated successfully using current thresholds");
            response.put("thresholds", currentThresholds);
            response.put("counts", tierCounts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error updating customer tiers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}