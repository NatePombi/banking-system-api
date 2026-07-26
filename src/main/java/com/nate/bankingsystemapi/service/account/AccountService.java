package com.nate.bankingsystemapi.service.account;

import com.nate.bankingsystemapi.dto.account.AccountDto;
import com.nate.bankingsystemapi.dto.account.PostAccountDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.mapper.AccountMapper;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository repo;
    private final UserRepository repoU;
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);


    /**
     * Creating Account for User
     *
     * @param postAccountDto a {@link PostAccountDto} object that has account details
     * @param username is username of logged-in User
     * @return a {@link AccountDto} object
     * @throws UserNotFoundException if user with given username was not found
     * @throws com.nate.bankingsystemapi.exception.CurrencyCodeMismatchException if given currency code is invalid
     */
    @Override
    public AccountDto createAccount(PostAccountDto postAccountDto, String username) {
        log.info("Attempting to create account for user: {}",username);

        //throws exception if User not found
        User user = findUser(username);

        //checks currency code if its valid, if not throws exception
        CurrencyCode code = CurrencyCode.getCurrencyCode(postAccountDto.getCurrency());

        //Creating Account object to store account details
        Account acc = Account.create(user,code);



        //saves the account entity to repo
        Account saved = repo.save(acc);
        log.info("Successfully created account for userId: {}",saved.getId());

        //Map Account entity to AccountDto object using mapper and return it
        return AccountMapper.toDto(saved);
    }

    /**
     * Get Account by id
     *
     * @param id the specified id of the account
     * @param username the username of the logged-in user
     * @return a {@link AccountDto} object
     * @throws UserNotFoundException if user with given username not found
     * @throws AccountNotFoundException if account with the given id was not found
     * @throws AccessDeniedException if user is not the owner of account
     */
    @Override
    public AccountDto getAccountById(Long id, String username) {
        log.info("Fetching Account by id: {}",id);

        //throws exception if User not found
        User user = findUser(username);


        //Fetches Account by id. throws exception if not found
        Account acc = repo.findByIdAndUserId(id,user.getId())
                .orElseThrow(()->{
                    log.error("Account not found: {}",id);
                    return new AccountNotFoundException(id);
                });


        log.info("Successfully fetched account by id: {}",id);
        //Map Account entity to AccountDto object using mapper and return it
        return AccountMapper.toDto(acc);
    }

    /**
     * Get Account by id
     *
     * @param accNum the specified id of the account
     * @param username the username of the logged-in user
     * @return a {@link AccountDto} object
     * @throws UserNotFoundException if user with given username not found
     * @throws AccountNotFoundException if account with the given account number was not found
     * @throws AccessDeniedException if user is not the owner of account
     */
    @Override
    public AccountDto getAccountByAccountNumber(Long accNum, String username) {
        log.info("Fetching Account by account number: {}",accNum);

        //throws exception if User not found
        User user = findUser(username);


        //Fetches Account by id. throws exception if not found
        Account acc = repo.findByAccountNum(accNum).orElseThrow(AccountNotFoundException::new);


        log.info("Successfully fetched account by account number: {}",accNum);
        //Map Account entity to AccountDto object using mapper and return it
        return AccountMapper.toDto(acc);
    }


    /**
     * Retrieves a paginated and sorted list of accounts
     *
     * @param username the specified username of logged-in user
     * @param page the page number that user wants to retrieve (0-based)
     * @param size the amount of items per page
     * @param sortBy the field the page is sorted by (e.g id,balcanceCent etc)
     * @param direction the way the pages are sorted (ascending or descending)
     * @return a paginated {@link Page} of a {AccountDto} object
     * @throws UserNotFoundException if given username is not found
     */
    @Override
    public Page<AccountDto> getAllUserAccount(String username, int page, int size, String sortBy, String direction) {
        //log.info("Attempting to fetch a paginated list of accounts for user: {}, page {}, size {}, sortBy {}, direction {}", user.getUsername(),page,size,sortBy,direction);

        //throws exception if User not found
        User user = findUser(username);

        //throws exception if User does not have any accounts
        if(!repo.existsByUser(user)) {
            log.warn("No accounts found for user: {}",user.getId());
            throw new AccountNotFoundException(user.getUsername());
        }

        //Configure sorting (ascending or descending)
        Sort sort = direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending(): Sort.by(sortBy).ascending();

        //Creates a Pageable object that defines page number, size and sorting
        Pageable pageable = PageRequest.of(page,size,sort);

        Page<Account> accountPage = repo.findByUser(user,pageable);


        log.info("Successfully fetched all account for user: {}",user.getId());
        return accountPage.map(AccountMapper::toDto);
    }


    /**
     * Fetching All Accounts for Admin
     *
     * @param username is username of logged in admin
     * @param page the page number that user wants. (0-based)
     * @param size the amount of items per page
     * @param sortBy the field the page is sorted by (e.g. id)
     * @param direction the way the pages are sorted (e.g. ascending or descending)
     * @return a {@link Page} of {@link AccountDto} object
     * @throws AccessDeniedException if admin is not found
     */
    @Override
    public Page<AccountDto> adminGetAllUserAccount(String username, int page, int size, String sortBy, String direction) {
        log.info("Attempting to fetch Accounts for Admin: {}",username);

        if(!isAdminExists(username,Role.ADMIN)){
            log.warn("No accounts found for admin: {}",username);
            throw new AccessDeniedException(String.format("Admin user %s not found",username));
        }

        Sort sort = direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending(): Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page,size,sort);

        Page<Account> accountPage = repo.findAll(pageable);
        log.info("Successfully fetched all accounts for Admin: {}",username);

        return accountPage.map(AccountMapper::toDto);
    }

    /**
     * Fetch Account for admin by Id
     *
     * @param id is id of account admin wants to get
     * @param username is username of admin that is logged in
     * @return a {@link AccountDto} object
     * @throws AccessDeniedException if admin not found
     * @throws AccountNotFoundException if account with id given was not found
     */
    @Override
    public AccountDto adminGetAccountById(Long id, String username) {
        log.info("Attempting to fetch Account by id: {}, for admin: {}",id,username);

        if(!isAdminExists(username,Role.ADMIN)){
            log.warn("No accounts found for admin: {}",username);
            throw new AccessDeniedException(String.format("Admin user %s not found",username));
        }

        Account account = repo.findById(id).orElseThrow(()->{
            log.error("Account not found. Id: {}",id);
            return new AccountNotFoundException(id);
        });

        log.info("Successfully retrieved Account for admin");
        return AccountMapper.toDto(account);
    }

    /**
     * Fetching Account by account number for Admin
     *
     * @param accNum is account number of account admin wants to retrieve
     * @param username is username of admin thats logged in
     * @return a {@link AccountDto} object
     * @throws AccessDeniedException if admin not found
     * @throws AccountNotFoundException if account with given account number was not found
     */
    @Override
    public AccountDto adminGetAccountByAccountNumber(Long accNum, String username) {
        log.info("Attempting to fetch account by account number: {}, for admin: {}",accNum,username);

        if(!isAdminExists(username,Role.ADMIN)){
            log.warn("No accounts found for admin: {}",username);
            throw new AccessDeniedException(String.format("Admin user %s not found",username));
        }

        Account account = repo.findByAccountNum(accNum).orElseThrow(()->{
            log.error("Account not found. Id: {}",accNum);
            return new AccountNotFoundException(accNum);
        });

        log.info("Successfully retrieved Account for admin");
        return AccountMapper.toDto(account);
    }

    private User findUser(String username){
        return repoU.findByUsername(username).orElseThrow(()->{
            log.error("User not found: {}",username);
            return new UserNotFoundException(username);
        });
    }

    private boolean isAdminExists(String username,Role role){
        return repoU.existsByUsernameAndRole(username,role);
    }




}
