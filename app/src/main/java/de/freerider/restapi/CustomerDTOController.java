package de.freerider.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.freerider.datamodel.Customer;
import de.freerider.repository.CustomerRepository;
import de.freerider.restapi.dto.CustomerDTO;

@RestController
public class CustomerDTOController implements CustomerDTOAPI {

	@Autowired
	private ApplicationContext context;
	@Autowired
	private CustomerRepository customerRepository;
	//
	private final HttpServletRequest request;

	/**
	 * Constructor
	 * 
	 * @param
	 * @param
	 */
	public CustomerDTOController(HttpServletRequest request) {

		this.request = request;
	}

	
	
	/*
	 * GET
	 * 
	 * Return CustomerDTP Array of people
	 * 
	 * @return CustomerDTO Array of people
	 */
	@Override
	public ResponseEntity<List<CustomerDTO>> getCustomers() {
		//
		ResponseEntity<List<CustomerDTO>> re = null;
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		List<CustomerDTO> customerDTOList = serializeCustomersWithDTO();
		re = new ResponseEntity<List<CustomerDTO>>(customerDTOList, HttpStatus.OK);
		return re;
	}

	
	/*
	 * GET
	 * 
	 * Return CustomerDTP Obj of customer
	 * 
	 * @param long id of customer
	 * @return CustomerDTP Obj of customer
	 */
	@Override
	public ResponseEntity<CustomerDTO> getCustomer(long id) {
		ResponseEntity<CustomerDTO> re = null;
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		Optional<Customer> customer = customerRepository.findById(id);

		if (customer.isPresent()) {

			CustomerDTO customerDTO = serializeCustomerWithDTO(customer.get());
			re = new ResponseEntity<CustomerDTO>(customerDTO, HttpStatus.OK);

		} else {

			re = new ResponseEntity<CustomerDTO>(HttpStatus.NOT_FOUND);
		}
		return re;
	}

	
	/*
	 * POST
	 * 
	 * Return CustomerDTP List of customers
	 * 
	 * @param List<CustomerDTO> containing to-be-posted customers in DTO
	 * @return CustomerDTP List of customers
	 */
	@Override
	public ResponseEntity<List<CustomerDTO>> postCustomers(List<CustomerDTO> dtos) {
		System.err.println(" POST /customers");
		ResponseEntity<List<CustomerDTO>> re = null;
		if (dtos == null)
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		for (CustomerDTO c : dtos) {

			Optional<Customer> customer = accept(c); // TBD rewrite accept for DTO!!

			if (customer.isEmpty()) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

			} else {
				if(customerRepository.findById(customer.get().getId()).isPresent()) {
					re =  new ResponseEntity<List<CustomerDTO>>( HttpStatus.CONFLICT);
				} else {
					customerRepository.save(customer.get());
					re =  new ResponseEntity<List<CustomerDTO>>( HttpStatus.CREATED);
				}
			}
				
		}
		return re;
	}

	
	
	@Override
	public ResponseEntity<List<CustomerDTO>> putCustomers(List<CustomerDTO> dtos) {
		
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		ResponseEntity<List<CustomerDTO>> re = null;
		List<CustomerDTO> deniedList = new ArrayList<>();
		
		for(CustomerDTO c : dtos) {
			System.out.println();
			c.print();
			Optional<Customer> customerOpt = accept(c);
			CustomerDTO.print(customerOpt);
			System.out.println();
			
			if ( customerOpt.isEmpty()) {
				deniedList.add(c);
				
			} else { 
				
				if(customerRepository.findById(customerOpt.get().getId()).isPresent()) {
					customerRepository.save(customerOpt.get());

				} else {
					deniedList.add(c);
				}
			}
		}
		
		if ( deniedList.size() == 0 ) {
			re = new ResponseEntity<List<CustomerDTO>>(HttpStatus.ACCEPTED);
		} else {
			re = new ResponseEntity<List<CustomerDTO>>(deniedList, HttpStatus.CONFLICT);
		}
		return re;
	}
	
	/**
	 * PUT /customers
	*/
//	@Override
//	public ResponseEntity<List<CustomerDTO>> putCustomers(@RequestBody List<CustomerDTO> dtos ) {
//	// TODO: replace by logger
//	System.err.println( request.getMethod() + " " + request.getRequestURI() ); //
//	dtos.stream().forEach( dto ->{
//		dto.print();
//		Optional<Customer> customerOpt = dto.create(); 
//		CustomerDTO.print( customerOpt );
//	});
//	return new ResponseEntity<>( null, HttpStatus.ACCEPTED );
//	}

	
	/*
	 * DELETE
	 * 
	 * Return HttpStatus of Action
	 * 
	 * @param long id of customer
	 * @return HttpStatus of Action
	 */
	@Override
	public ResponseEntity<?> deleteCustomer(long id) {
		System.err.println( "DELETE /customers/" + id);
		if(customerRepository.existsById(id)) {
			customerRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	
	/*
	 *  Serializes Customers in customerRepository customer -> customerDTO
	 *  @return List<CustomerDTO>
	 */
	public List<CustomerDTO> serializeCustomersWithDTO() {
		//
		final Iterable<Customer> customers = customerRepository.findAll();
		List<CustomerDTO> customersDTO = new ArrayList<>();
		customers.forEach(c -> {
			customersDTO.add(new CustomerDTO(c));
		});
		return customersDTO;
	}

	
	/*
	 *  serializes specific customer -> customerDTO
	 *  
	 *  @param customer c 
	 *  @return CustomerDTO 
	 */
	
	public CustomerDTO serializeCustomerWithDTO(Customer c) {
		//
		return new CustomerDTO(c);
	}

	
	/*
	 * Creates Optional<Customer> from customerDTO
	 * 
	 * @param CustomerDTO customerDTO
	 * @return Optional<Customer>
	 */
	private Optional<Customer> accept( CustomerDTO customerDTO){
		Optional<Customer> optCustomer = Optional.empty();
		
		if(customerDTO == null) {
			
			throw new IllegalArgumentException();
		}
		
		else {
			
			optCustomer = customerDTO.create();
		}
		
		return optCustomer; 
	}
}
