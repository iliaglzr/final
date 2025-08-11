package org.example;

import DAO.EmailDAO;
import DAO.EmailRecipientDao;
import DAO.UserDAO;
import Util.HibernateUtil;
import org.example.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EmailService {
    private static final EmailDAO emailDao = new EmailDAO();
    private static final EmailRecipientDao recipientDao = new EmailRecipientDao();
    private static final UserDAO userDao = new UserDAO();

    private String generateEmailCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }


    public String sendEmail(User sender, Set<User> recipients, String subject, String body) {
        Transaction tx = null;
        String code = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User attachedSender = session.get(User.class, sender.getId());
            if (attachedSender == null) {
                attachedSender = sender;
            }

            Email email = new Email();
            email.setSender(attachedSender);
            email.setSubject(subject);
            email.setBody(body);
            email.setSentAt(LocalDateTime.now());
            code = generateEmailCode();
            email.setCode(code);
            session.save(email);

            for (User r : recipients) {
                if (r == null) continue;
                User attachedRecipient = session.get(User.class, r.getId());
                if (attachedRecipient == null) {
                    System.out.println("Recipient user not found in DB: " + r.getEmail());
                    continue;
                }
                EmailRecipient er = new EmailRecipient();
                er.setEmail(email);
                er.setRecipient(attachedRecipient);
                er.setRead(attachedRecipient.getId().equals(sender.getId()));
                er.setBody(email.getBody());
                er.setSubject(email.getSubject());
                session.save(er);
            }
            tx.commit();
            return code;
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            ex.printStackTrace();
            return null;
        }
    }


    public List<EmailRecipient> getInbox(int userId) {
        return emailDao.getAllEmail(userId);
    }

    public List<EmailRecipient> getUnreadInbox(int userId) {
        return emailDao.getUnreadMessages(userId);
    }

    public List<Email> getSentEmails(int userId) {
        return emailDao.getSentEmails(userId);
    }

    public void readEmailByCode(String currentUserEmail, String code) {
        Email email = emailDao.findByCode(code);

        if (email == null) {
            System.out.println("Email not found.");
            return;
        }

        boolean isSender = email.getSender().getEmail().equals(currentUserEmail);
        boolean isRecipient = email.getEmailRecipients().stream()
                .anyMatch(u -> u.getEmail().equals(currentUserEmail));

        if (!isSender && !isRecipient) {
            System.out.println("You cannot read this email.");
            return;
        }
        System.out.println("Code: " + email.getCode());
        System.out.print("Recipient(s): ");
        String recs = email.getEmailRecipients().stream()
                .map(er -> er.getRecipient().getEmail())
                .collect(Collectors.joining(", "));


        System.out.println(recs);
        System.out.println("Subject: " + email.getSubject());
        System.out.println("Date: " + email.getSentAt());
        System.out.println();
        System.out.println(email.getBody());

        recipientDao.markAsRead(currentUserEmail);
    }


    public Email replyToEmail(String originalCode, String body, User currentUser) {
        Transaction tx = null;
        Email reply = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Email original = session.createQuery("FROM Email WHERE code = :code", Email.class)
                    .setParameter("code", originalCode)
                    .uniqueResult();

            if (original == null) {
                System.out.println("No email found with that code.");
                return null;
            }

            boolean isSender = original.getSender().getId().equals(currentUser.getId());
            boolean isRecipient = original.getEmailRecipients()
                    .stream()
                    .anyMatch(r -> r.getRecipient().getId().equals(currentUser.getId()));

            if (!isSender && !isRecipient) {
                System.out.println("You cannot reply to this email.");
                return null;
            }

            reply = new Email();
            reply.setSender(currentUser);
            reply.setSubject("[Re] " + original.getSubject());
            reply.setBody(body);
            reply.setSentAt(LocalDateTime.now());
            reply.setCode(generateEmailCode());


            Set<EmailRecipient> replyRecipients = new HashSet<>();

            if (!original.getSender().getId().equals(currentUser.getId())) {
                EmailRecipient replyRecipient = new EmailRecipient();
                replyRecipient.setEmail(reply);
                replyRecipient.setRecipient(original.getSender());
                replyRecipient.setRead(false);
                replyRecipients.add(replyRecipient);
            } else {
                for (EmailRecipient originalRecipient : original.getEmailRecipients()) {
                    EmailRecipient replyRecipient = new EmailRecipient();
                    User recipientUser = originalRecipient.getRecipient();
                    replyRecipient.setEmail(reply);
                    replyRecipient.setRecipient(recipientUser);
                    replyRecipient.setRead(recipientUser.getId().equals(currentUser.getId()));
                    replyRecipients.add(replyRecipient);
                }
            }

            reply.setEmailRecipients(replyRecipients);




            reply.setEmailRecipients(replyRecipients);
            session.persist(reply);
            tx.commit();
            System.out.println("your email sent to:"+ original.getSender().getName()  );
            System.out.println("email's code :"+ reply.getCode());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return reply;
    }


    public Email forwardEmail(String originalCode, List<String> recipientEmails, User currentUser) {
        Transaction tx = null;
        Email forward = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Email original = session.createQuery("FROM Email WHERE code = :code", Email.class)
                    .setParameter("code", originalCode)
                    .uniqueResult();
            if (original == null) {
                System.out.println("No email found with that code.");
                return null;
            }

            boolean allowed = original.getSender().getId().equals(currentUser.getId());
            if (!allowed) {
                System.out.println("You cannot forward this email.");
                return null;
            }

            List<User> newRecipients = session.createQuery("FROM User WHERE email IN :emails", User.class)
                    .setParameter("emails", recipientEmails)
                    .getResultList();

            if (newRecipients.isEmpty()) {
                System.out.println("No valid recipients found.");
                return null;
            }

            forward = new Email();
            forward.setSender(currentUser);
            forward.setSubject("[Fw] " + original.getSubject());
            forward.setBody(original.getBody());
            forward.setSentAt(LocalDateTime.now());
            forward.setCode(generateEmailCode());

            Set<EmailRecipient> forwardRecipients = new HashSet<>();
            for (User recipient : newRecipients) {
                EmailRecipient er = new EmailRecipient();
                er.setEmail(forward);
                er.setRecipient(recipient);
                er.setRead(false);
                forwardRecipients.add(er);
            }
            forward.setEmailRecipients(forwardRecipients);

            session.persist(forward);
            tx.commit();

            System.out.println("your email sent! "  );
            System.out.println("email's code :"+ forward.getCode());

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }

        return forward;
    }





}

