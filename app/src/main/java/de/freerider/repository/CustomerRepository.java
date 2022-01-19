package de.freerider.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.freerider.datamodel.Customer;

@Repository
public class CustomerRepository implements CrudRepository<Customer, Long> {

	HashMap<Long, Customer> hashMap = new HashMap<Long, Customer>();

	@Override
	public <S extends Customer> S save(S entity) {
		// If entity is null, throw Exception, else put it into hashMap
		if (entity == null) {
			throw new IllegalArgumentException("The given entity is null");
		} else {
			Long id = entity.getId();		
			if(id < 0L) {
				id = Long.valueOf((long)(Math.random() * 10000.0));
				while(hashMap.containsKey(id)) {
					id = Long.valueOf((long)(Math.random() * 10000.0));
				}
			}
			entity.setId(id);
			hashMap.put(id, entity);
		}
		return entity;
	}

	@Override
	public <S extends Customer> Iterable<S> saveAll(Iterable<S> entities) {
		// if iterable entities is null, throw Exception, else proceed to put its
		// contains to hashMap
		if (entities == null) {
			throw new IllegalArgumentException("The given Iterable entities is null");
		} else {
			// for each Customer in entities, if it is null, throw Exception, else put into
			// hashMap
			for (Customer c : entities) {
				if (c == null) {
					throw new IllegalArgumentException("The given Iterable entities contains an Element that is null");
				} else {
					hashMap.put(c.getId(), c);
				}
			}
		}
		return entities;
	}

	@Override
	public boolean existsById(Long id) {
		// if long id is null, throw Exception, else check hashMap for given id
		if (id == null) {
			throw new IllegalArgumentException("The given long id is null");
		} else {
			if (hashMap.containsKey(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Optional<Customer> findById(Long id) {
		// if long id is null, throw Exception, else try to find Customer by id
		Optional<Customer> optFoundCustomer = Optional.empty();
		if (id == null) {
			throw new IllegalArgumentException("The given long id is null");
		} else {
			optFoundCustomer = Optional.ofNullable(hashMap.get(id));
		}
		return optFoundCustomer;
	}

	@Override
	public Iterable<Customer> findAll() {
		return hashMap.values();
	}

	@Override
	public Iterable<Customer> findAllById(Iterable<Long> ids) {
		ArrayList<Customer> foundById = new ArrayList<Customer>();
		if (ids == null) {
			throw new IllegalArgumentException("Given Iterable ids is null");
		} else {
			for (Long id : ids) {
				if (id == null) {
					throw new IllegalArgumentException("Given id is null");
				} else {
					if (hashMap.containsKey(id)) {
						foundById.add(hashMap.get(id));
					}
				}
			}
		}
		return foundById;
	}

	@Override
	public long count() {
		return hashMap.size();
	}

	@Override
	public void deleteById(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Given id is null");
		} else {
			hashMap.remove(id);
		}
	}

	@Override
	public void delete(Customer entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Given Customer entity is null");
		} else {
			hashMap.remove(entity.getId());
		}
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids) {
		if (ids == null) {
			throw new IllegalArgumentException("Given Iterable ids is null");
		} else {
			for (Long id : ids) {
				if (id == null) {
					throw new IllegalArgumentException("Given id is null");
				} else {
					hashMap.remove(id);
				}
			}
		}
	}

	@Override
	public void deleteAll(Iterable<? extends Customer> entities) {
		if (entities == null) {
			throw new IllegalArgumentException("Given Iterable entities is null");
		} else {
			for (Customer ent : entities) {
				if (ent == null) {
					throw new IllegalArgumentException("Given entity is null");
				} else {
					hashMap.remove(ent.getId());
				}
			}
		}

	}

	@Override
	public void deleteAll() {
		hashMap.clear();
	}
}
