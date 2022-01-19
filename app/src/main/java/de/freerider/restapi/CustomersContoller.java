package de.freerider.restapi;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.freerider.datamodel.Customer;
import de.freerider.repository.CustomerRepository;

import com.fasterxml.jackson.core.type.TypeReference;



class CustomersContoller implements CustomersAPI {
	//
	@Autowired
    private ApplicationContext context;
	@Autowired
	private CustomerRepository customerRepository;
	//
	private final ObjectMapper objectMapper;
	//
	private final HttpServletRequest request;


	/**
	 * Constructor.
	 * 
	 * @param objectMapper entry point to JSON tree for the Jackson library
	 * @param request HTTP request object
	 */
	public CustomersContoller( ObjectMapper objectMapper, HttpServletRequest request ) {
		this.objectMapper = objectMapper;
		this.request = request;
	}


	/**
	 * GET /people
	 * 
	 * Return JSON Array of people (compact).
	 * 
	 * @return JSON Array of people
	 */
	@Override
	public ResponseEntity<List<?>> getCustomers() {
		//
		ResponseEntity<List<?>> re = null;
		System.err.println( request.getMethod() + " " + request.getRequestURI() );   
		try {
			ArrayNode arrayNode = serializeCustomersAsJSON();
			ObjectReader reader = objectMapper.readerFor( new TypeReference<List<ObjectNode>>() { } );
			List<String> list = reader.readValue( arrayNode );
			//
			re = new ResponseEntity<List<?>>( list, HttpStatus.OK );

		} catch( IOException e ) {
			re = new ResponseEntity<List<?>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return re;
	}


	/**
	 * GET /people/pretty
	 * 
	 * Return JSON Array of people (pretty printed with indentation).
	 * 
	 * @return JSON Array of people
	 */
	@Override
	public ResponseEntity<?> getCustomer(long id) {
		//
		ResponseEntity<ObjectNode> re = null;
		
		Optional<Customer> customer = customerRepository.findById(id);
		
		System.err.println( request.getMethod() + " " + request.getRequestURI() );   

		if(customer.isPresent()) {
			ObjectNode objectNode = serializeCustomerAsJSON(customer.get());
			re = new ResponseEntity<ObjectNode>( objectNode, HttpStatus.OK );
			
		} else {			
			re = new ResponseEntity<ObjectNode>(HttpStatus.NOT_FOUND);
		}
		return re;
	}

	
	private ObjectNode serializeCustomerAsJSON(Customer c) {
		//
		ObjectNode objectNode = objectMapper.createObjectNode();
		//
		StringBuffer sb = new StringBuffer();
		c.getContacts().forEach( contact -> sb.append( sb.length()==0? "" : "; " ).append( contact ) );
		
		objectNode.put( "id", c.getId());
		objectNode.put( "name", c.getLastName() );
		objectNode.put( "first", c.getFirstName() );
		objectNode.put( "contacts", sb.toString() );
		return objectNode;
	}
	
	
	private ArrayNode serializeCustomersAsJSON() {
		//
		final Iterable<Customer > customers = customerRepository.findAll();
		ArrayNode arrayNode = objectMapper.createArrayNode();
		//
		customers.forEach( c -> {
			StringBuffer sb = new StringBuffer();
			c.getContacts().forEach( contact -> sb.append( sb.length()==0? "" : "; " ).append( contact ) );
			arrayNode.add(
				objectMapper.createObjectNode()
					.put("id", c.getId())
					.put( "name", c.getLastName() )
					.put( "first", c.getFirstName() )
					.put( "contacts", sb.toString() )
			);
		});
		return arrayNode;
	}


	@Override
	public ResponseEntity<List<?>> postCustomers( Map<String, Object>[] jsonMap ) {
		System.err.println( "POST /customers" );
		ResponseEntity<List<?>> re = null;
		if( jsonMap == null) return new ResponseEntity<>( null, HttpStatus.BAD_REQUEST );
		
		for ( Map<String, Object> kvpairs : jsonMap ) {
			
			Optional<Customer> customer = accept(kvpairs);
			
			if(customer.isEmpty()) {
				re =  new ResponseEntity<List<?>>( HttpStatus.BAD_REQUEST);
			}
			if(customer.isPresent()) {
				if(customerRepository.findById(customer.get().getId()).isPresent()) {
					re =  new ResponseEntity<List<?>>( HttpStatus.CONFLICT);
				} else {
					customerRepository.save(customer.get());
					re =  new ResponseEntity<List<?>>( HttpStatus.CREATED);
				}
			}
		}
		return re;
	}


	@Override
	public ResponseEntity<List<?>> putCustomers(Map<String, Object>[] jsonMap) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResponseEntity<?> deleteCustomer(long id) {
		System.err.println("DELETE /customers/"+ id);
		if(customerRepository.existsById(id)){
			customerRepository.deleteById(id);
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED);			
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}
	
	
	private Optional<Customer> accept( Map<String, Object> kvpairs) {
		long id;
		String first;
		String name;
		String contacts;
		Customer customer = new Customer();
		
		if(kvpairs == null) throw new IllegalArgumentException();
			
		else {
			
			Object valueID = kvpairs.get("id");
			System.out.println(valueID);
			
			if ( valueID == null) {
				id = Long.valueOf((long)(Math.random() * 10000.0));
			} else {
				id = (Integer) valueID;
				if( id <= 0L ) {
					return Optional.empty();
				}
			}
			
			Object valueFirst = kvpairs.get("first");
			if ( valueFirst == null) {
				return Optional.empty();
			} else {
				first = (String) valueFirst;
			}
			
			Object valueName = kvpairs.get("name");
			if ( valueName == null) {
				return Optional.empty();
			} else {
				name = (String) valueName;
			}
			customer.setName(first, name);
			
			
			Object valueContacts = kvpairs.get("contacts");
			if( valueContacts != null ) {
				contacts = (String) valueContacts;
				String[] contactsArr = contacts.split(";");
				for(String contact : contactsArr) {
					customer.addContact(contact);
				}
			} 
			
			customer.setId(id);
			customer.setName(first, name);
			
			return Optional.ofNullable(customer);
		}
	}
}
