package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.mapper.AccountMapper;
import com.nate.bankingsystemapi.model.Account;
import com.nate.bankingsystemapi.model.Role;
import com.nate.bankingsystemapi.model.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository repo;
    private final UserRepository repoU;
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);


    /**
     * Creating Account for User
     *
     * @param postAccountDto a {@link PostAccountDto} object that has account details
     * @param username the specified username of the logged-in user
     * @return a {@link AccountDto} object
     * @throws UserNotFoundException if user with given username was not found
     */
    @Override
    public AccountDto createAccount(PostAccountDto postAccountDto, String username) {
        log.info("Creating account for user: {}",username);

        //Fetching User, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(()->{
                    log.error("User not found: {}",username);
                    return new UserNotFoundException(username);
                });

        //Creating Account entity to store account details
        Account acc = new Account();
        acc.setBalance(postAccountDto.getBalance());
        acc.setCurrency(postAccountDto.getCurrency());
        acc.setUser(user);

        //saves the account entity to repo
        repo.save(acc);

        //Map Account entity to AccountDto object using mapper and return it
        return AccountMapper.toDto(acc);
    }

    /**
     * Get Account by id
     *
     * @param id the specified id of the account
     * @param username the username of the logged in user
     * @return a {@link AccountDto} object
     * @throws UserNotFoundException if user with given username not found
     * @throws AccountNotFoundException if account with the given id was not found
     * @throws AccessDeniedException if user is not the owner of account
     */
    @Override
    public AccountDto getAccountById(Long id, String username) {
        log.info("Fetching Account by id: {}",id);

        //Fetches User by username, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(()->{
                    log.error("User not found: {}",username);
                    return new UserNotFoundException(username);
                });

        //Fetches Account by id. throws exception if not found
        Account acc = repo.findById(id)
                .orElseThrow(()->{
                    log.error("Account not found: {}",id);
                    return new AccountNotFoundException(id);
                });

        //if user is not owner of account or not admin, throws exception
        if(!acc.getUser().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)){
            log.error("Unauthorized access for this product: {}",id);
            throw new AccessDeniedException("Not Authorized");
        }

        //Map Account entity to AccountDto object using mapper and return it
        return AccountMapper.toDto(acc);
    }


    /**
     * Retrieves a paginated and sorted list of accounts
     *
     * @param username the specified username of logged in user
     * @param page the page number that user wants to retrieve (0-based)
     * @param size the amount of items per page
     * @param sortBy the field field the page is sorted by (e.g id,balcanceCent etc)
     * @param direction the way the pages are sorted (ascending or descending)
     * @return a paginated {@link Page} of a {AccountDto} object
     * @throws UserNotFoundException if given username is not found
     */
    @Override
    public Page<AccountDto> getAllUserAccount(String username, int page, int size, String sortBy, String direction) {
        log.info("Fetch a paginated list of accounts for user: {}, page {}, size {}, sortBy {}, direction {}",username,page,size,sortBy,direction);

        //Fetches user by username, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(()-> {
                    log.error("User not found: {}",username);
                    return new UserNotFoundException(username);
                });

        //Configure sorting (ascending or descending)
        Sort sort = direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending(): Sort.by(sortBy).ascending();

        //Creates a Pageable object that defines page number, size and sorting
        Pageable pageable = PageRequest.of(page,size,sort);

        Page<Account> accountPage;

        if(user.getRole().equals(Role.ADMIN)){
            accountPage = repo.findAll(pageable);
        }
        else {
            accountPage = repo.findByUserUsername(username,pageable);
        }


        return accountPage.map(AccountMapper::toDto);
    }


}
