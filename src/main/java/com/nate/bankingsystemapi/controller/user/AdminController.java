package com.nate.bankingsystemapi.controller.user;

import com.nate.bankingsystemapi.dto.PaginatedResponse;
import com.nate.bankingsystemapi.dto.user.UserDto;
import com.nate.bankingsystemapi.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Controller", description = "End points for managing admin requests")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;

    @Operation(summary = "Fetching a Pagniated list of all users for admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "successfully returned list of all users"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
    })
    @GetMapping("/fetch")
    public ResponseEntity<PaginatedResponse<UserDto>> adminGetAllUser(@AuthenticationPrincipal(expression = "username") String username,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "5") int size,
                                                                      @RequestParam(defaultValue = "id") String sortBy,
                                                                      @RequestParam(defaultValue = "desc") String direction){

        Page<UserDto> responses = userService.adminGetAllUser(username,page,size,sortBy,direction);

        PaginatedResponse<UserDto> paginatedResponse = new PaginatedResponse<>(
                responses.getContent(),
                page,
                responses.getTotalPages(),
                responses.getTotalElements(),
                responses.hasContent()
        );

        return ResponseEntity.ok(paginatedResponse);
    }

    @Operation(summary = "Fetching users by id for admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "successfully fetched user by account"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "404",description = "user not found"),
    })
    @GetMapping("/fetch/{id}")
    public ResponseEntity<UserDto> adminGetUserById(@PathVariable long id,@AuthenticationPrincipal(expression = "username") String username){
        return ResponseEntity.ok(userService.adminGetUserById(id,username));
    }
}
