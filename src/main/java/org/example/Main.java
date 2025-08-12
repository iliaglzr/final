package org.example;

import DAO.EmailRecipientDao;
import DAO.UserDAO;
import org.example.*;

import java.util.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static EmailService emailService = new EmailService();
    private static UserService userService = new UserService();
    private static UserDAO userDAO = new UserDAO();
    private static EmailRecipientDao emailRecipientDao = new EmailRecipientDao();
    private static User currentUser = null;

    public static void main(String[] args) {
        while (true) {
            if (currentUser == null) {
                handleLoginOrSignUp();
            } else {
                showWelcomeAndCommands();
                String cmd = scanner.nextLine().trim().toLowerCase();
                switch (cmd) {
                    case "s":
                    case "send":
                        sendEmail();
                        break;
                    case "v":
                    case "view":
                        viewEmails();
                        break;
                    case "r":
                    case "reply":
                        System.out.print("Code: ");
                        String replyCode = scanner.nextLine().trim();
                        System.out.print("Body: ");
                        String replyBody = scanner.nextLine().trim();

                        try {
                            emailService.replyToEmail(replyCode, replyBody, currentUser);
                        } catch (Exception e) {
                            System.out.println("Failed to reply to email: " + e.getMessage());
                        }
                        break;

                    case "f":
                    case "forward":
                        System.out.print("Code: ");
                        String forwardCode = scanner.nextLine().trim();
                        System.out.print("Recipient(s): ");
                        String forwardRecipientsLine = scanner.nextLine().trim();
                        List<String> forwardRecipients = Arrays.stream(forwardRecipientsLine.split(","))
                                .map(String::trim)
                                .toList();

                        try {
                            emailService.forwardEmail(forwardCode, forwardRecipients, currentUser);
                        } catch (Exception e) {
                            System.out.println("Failed to forward email: " + e.getMessage());
                        }
                        break;


                    default:
                        System.out.println("Invalid command.");
                }
            }
        }
    }

    private static void handleLoginOrSignUp() {
        System.out.print("[L]ogin, [S]ign up: ");
        String input = scanner.nextLine().trim();
        if (input.equals("L") || input.equals("Login")) {
            login();
        } else if (input.equals("S") || input.equals("sign up") || input.equals("signup")) {
            signUp();
        } else {
            System.out.println("Invalid command.");
        }
    }

    private static void login() {
        System.out.print("Email: ");
        String email = fixEmail(scanner.nextLine().trim());

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = userService.login(email, password);
            currentUser = user;
            System.out.println("Welcome back, " + currentUser.getName() + "!");

            List<EmailRecipient> unreadEmails = emailService.getUnreadInbox(currentUser.getId());
            if (unreadEmails.isEmpty()) {
                System.out.println("No unread emails.");
            } else {
                for (EmailRecipient er : unreadEmails) {
                    Email emails = er.getEmail();
                    System.out.printf("Code: %s | Subject: %s|Body : %s| From: %s | Date: %s\n",
                            emails.getCode(), emails.getSubject(), emails.getBody(), emails.getSender().getEmail(), emails.getSentAt());
                }

            }


        }catch (RuntimeException e) {
            System.out.println("Invalid email or password.");
        }
    }


    private static void signUp() {
        while (true) {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = fixEmail(scanner.nextLine().trim());

            System.out.print("Password: ");
            String password = scanner.nextLine();

            boolean status = userService.register(name, email, password);
            if (status = true) {
                System.out.println("Your new account is created.");
                System.out.println("Go ahead and login!");
                break;
            } else {
                System.out.println("Please try again.");
            }
        }
    }

    private static String fixEmail(String email) {
        if (!email.contains("@")) {
            email += "@milou.com";
        }
        return email.toLowerCase();
    }

    private static void showWelcomeAndCommands() {

        System.out.println("[S]end, [V]iew, [R]eply, [F]orward: ");
    }

    private static void sendEmail() {

        System.out.print("Recipients:");
        String recipientInput = scanner.nextLine().trim();
        String[] usernames = recipientInput.split(",");

        Set<User> recipients = new HashSet<>();
        for (String username : usernames) {
            User user = userDAO.FindByUsername(username.trim());
            if (user != null) {
                recipients.add(user);
            } else {
                System.out.println("User not found: " + username.trim());
            }
        }

        if (recipients.isEmpty()) {
            System.out.println("No valid recipients found. Email not sent.");
            return;
        }



        System.out.print("Subject: ");
        String subject = scanner.nextLine().trim();

        System.out.print("Body: ");
        String body = scanner.nextLine().trim();

        String code = emailService.sendEmail(currentUser,recipients ,subject,body);
        if (code != null) {
            System.out.println("Successfully sent your email.");
            System.out.println("Code: " + code);
        } else {
            System.out.println("Failed to send email. Check recipients.");
        }
    }

    private static void viewEmails() {
        System.out.print("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode: ");
        String choice = scanner.nextLine().trim().toLowerCase();

        switch (choice) {
            case "a":
                List<Email> allEmails = emailService.getInbox(currentUser.getEmail());
                if (allEmails.isEmpty()) {
                    System.out.println("No unread emails.");
                } else {
                    for (Email er : allEmails) {

                        System.out.printf("Code: %s | Subject: %s | From: %s | Date: %s\n",
                                er.getCode(), er.getSubject(), er.getSender().getEmail(), er.getSentAt());
                    }
                }

                break;
            case "u":
                List<EmailRecipient> unreadEmails = emailService.getUnreadInbox(currentUser.getId());
                if (unreadEmails.isEmpty()) {
                    System.out.println("No unread emails.");
                } else {
                    for (EmailRecipient er : unreadEmails) {
                        emailRecipientDao.markAsRead(currentUser.getEmail());
                        Email email = er.getEmail();
                        System.out.printf("Code: %s | Subject: %s |Body : %s| From: %s | Date: %s\n",
                                email.getCode(), email.getSubject(),email.getBody(), email.getSender().getEmail(), email.getSentAt());
                    }
                }

                break;
            case "s":
                List<Email>sentEmail =emailService.getSentEmails(currentUser.getId());
                if (sentEmail.isEmpty()) {
                    System.out.println("No unread emails.");
                } else {
                    for (Email email : sentEmail) {
                    System.out.println("code :"+email.getCode()+ "|Subject :" + email.getSubject()+"|Body :"+email.getBody()+"|Date :"+ email.getSentAt());
                    }
                }
                break;
            case "c":
                System.out.print("Code: ");
                String code = scanner.nextLine().trim().toLowerCase();
                emailService.readEmailByCode(currentUser.getEmail(), code);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }




}
