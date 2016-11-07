import java.util.Scanner;

/*
 * Runnable class that checks the terminal. If 'k' is typed, sets a local variable 'kill' to true and stops running.
 */
public class InputChecker implements Runnable
{
	// Instance variables
    private Scanner scanner;
    public Boolean kill;
    
    /**
     * Constructor for objects of class Server
     */
    public InputChecker()
    {
        scanner = new Scanner(System.in);
        kill = false;
    }
    
    /*
     * @override
     * If the user inputs 'k' the thread will kill
     */
    public void run()
    {
        System.out.println("Enter k to kill this host");
        while(!kill)
        {
            String input = scanner.next();
            kill = input.equals("k");
        }
    }
}