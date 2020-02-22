# @begin EmphasizedHelloWorld @desc Display one or more greetings to the user.
# @in provided_greeting
# @in provided_emphasis
# @param emphasis_count
# @out displayed_greeting @desc Greeting displayed to user.

def greet_user(greeting, emphasis='!', count=1):

    # @begin emphasize_greeting @desc Add emphasis to the provided greeting
    # @in greeting @as provided_greeting
    # @in emphasis @as provided_emphasis
    # @param count @as emphasis_count
    # @out greeting @as emphasized_greeting @desc emphasized greeting as output
    if (count > 0):
        for i in range(0, count):
            greeting = greeting + emphasis
    # @end emphasize_greeting

    # @begin print_greeting @desc Greet the user with the emphasized message.
    # @in greeting @as emphasized_greeting @desc emphasized greeting as input
    # @out greeting @as displayed_greeting @file stream:stdout
    print(greeting)
    # @end print_greeting

# @end EmphasizedHelloWorld

if __name__ == '__main__':
    first_greeting = 'Hello World'
    greet_user(first_greeting)
    second_greeting = 'Goodbye World'
    greet_user(second_greeting, '?', 2)
    third_emph_count = 3
    greet_user("Back again", '?!', third_emph_count)
