import React, { useState } from 'react';
import{ Grid, Paper, Typography, Avatar, Box, TextField, Button } from '@mui/material';


const WrapperStyles = "w-[100%] md:w-[70%] max-w-[1300px] m-auto";

const Wrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    return (
        <div className={WrapperStyles}>{children}</div>
    );
}

export default Wrapper;